<script setup>
import axios from 'axios';


//测试代码
import { defineComponent, reactive, ref, onMounted } from 'vue';
import { ElSelect, ElMessage, ElMessageBox } from 'element-plus';
import * as echarts from 'echarts';
import {Document, Menu as IconMenu,Location,Setting,} from '@element-plus/icons-vue'


//设置颜色映射
 const mapcolor = new Map([
  ["METHOD", '#FFD04F'],
  ["TYPE_DECL", '#0B848C'],
  ["ANNOTATION", '#4CC8F5'],
  ["SELECTED", '#C9181E'],
 ])

//定义选择目标的绑定变量
  //这里的array用于存图中的信息
  var node_array = [

  ]
  var old_node_array = []
  var method_node_array = []
  var edge_array = []
  var method_edge_array = []
  var old_edge_array = []
  //这里的node_data用于存放相应的节点
  /*
  const Node = reactive({
    label:'',
    name:'',
    code:'',
    fullname:'',
    filename:'',
    id: '',
  })
  */
  var node_data = []
  var old_node_data = []
  var method_node_data = []

  const radiodisabled = ref(false)
  const active = ref(0)
  //定义已经选择的class
  var classselected = null

  var old_selectindex = -1 //上一个被选中的节点

  const mycharts = ref(null)
  onMounted(() => {
    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(mycharts.value);
    var option;
    myChart.showLoading();
    myChart.hideLoading();
    myChart.setOption(
      (option = {
        title: {
          text: ''
        },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'none',
            // progressiveThreshold: 700,
            data: node_array,
            edges: edge_array,
            emphasis: {
              focus: 'adjacency',
              label: {
                position: 'right',
                show: true
              }
            },
            edgeSymbol: ['', 'arrow'],
            roam: true,
            draggable: true,
            lineStyle: {
              width: 0.5,
              curveness: 0.3,
              opacity: 0.7
            }
          }
        ]
      }),
      true
    );

    //保存拖拽节点位置
    myChart.on("mouseup", params => {
      let index = params.dataIndex
      option.series[0].data[index].x = myChart._chartsViews[0]._symbolDraw._data._itemLayouts[index][0]
      option.series[0].data[index].y = myChart._chartsViews[0]._symbolDraw._data._itemLayouts[index][1]
      //node_array也要更新
      node_array[index].x = option.series[0].data[index].x
      node_array[index].y = option.series[0].data[index].y
    })

    myChart.on("dblclick", params => {
        //先把选中节点变色
      option = {
        title: {
          text: ''
        },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'none',
            // progressiveThreshold: 700,
            data: node_array,
            edges: edge_array,
            emphasis: {
              focus: 'adjacency',
              label: {
                position: 'right',
                show: true
              }
            },
            edgeSymbol: ['', 'arrow'],
            roam: true,
            draggable: true,
            lineStyle: {
              width: 0.5,
              curveness: 0.3,
              opacity: 0.7
            }
          }
        ]
      }
        let index = params.dataIndex
        if(old_selectindex != -1 && old_selectindex != index){
            option.series[0].data[old_selectindex].itemStyle.color = mapcolor.get(node_data[old_selectindex].label)

        }
        option.series[0].data[index].itemStyle.color = mapcolor.get('SELECTED')
        old_selectindex=index
        myChart.setOption(option)
        //然后在节点属性栏显示
        Node.label = node_data[index].label
        Node.name = node_data[index].name
        Node.code = node_data[index].code
        Node.fullname = node_data[index].fullname
        Node.filename = node_data[index].filename
        Node.id = node_data[index].id
    })
  }
);




const selectvalue = ref('Object1_update')

const selectclass = ref('')
const selectmethod = ref('')


//定义表单输入的东西
const form1 = reactive({
  class: '',
  method: '',
  isup: 1,
})

const form1_update = reactive({
  package: '',
  class: '',
  method: '',
  isdown: false,
  isclass: true,
  ismethod: true,
})

const form2 = reactive({
  url: '',
})

const form3 = reactive({
  database: '',//database暂时不用
  table: '',
  field: '',
})


//定义选择目标的选项，包括key和标签
const options = [
  {
    value: 'Object1',
    label: '目标1',
  },
  {
    value: 'Object1_update',
    label: "目标1优化"
  },
  {
    value: 'Object2',
    label: '目标2',
  },
  {
    value: 'Object3',
    label: '目标3',
  },
]

const classoption = reactive([

])

const methodoption = reactive([

])

//这里填写获取到的PathArray结构
/*
  Method结构：
  neo4jPath:
    List<neo4jNode> pathMember
    Integer pathLen
  neo4jNode:
    label
    name
    code
    fullname

    要找到Node节点中的某个属性
    即PathArray.forEach( path => {
      path.pathMember.forEach( node => {
        node.label
        node.name
        node.code
        node.fullname
        node.filename
      })
    })
*/

const Node = reactive({
  label:'',
  name:'',
  code:'',
  fullname:'',
  filename:'',
  id: '',
})

const PathArray = reactive([])

const handleSelect = (key) =>{
  //清空关系图状态
  onReset1_update()
  onReset2()
  onReset3()


  console.log(key)
  if(key == '1-1') {
    selectvalue.value = 'Object1_update'

  }
  else if(key == '1-2'){
    selectvalue.value = 'Object2'
  }  
  else if(key == '1-3'){
    selectvalue.value = 'Object3'
  }
  else{

  }  
}


//查找给出的包下的类
const getClass = () => {
  old_selectindex = -1
  radiodisabled.value = true
  var myChart = echarts.init(mycharts.value);
  myChart.showLoading()
  classoption.splice(0,classoption.length)
  node_array.splice(0,node_array.length)
  node_data.splice(0,node_data.length)
  edge_array.splice(0,edge_array.length)
  old_node_array.splice(0,old_node_array.length)
  old_node_data.splice(0,old_node_data.length)
  old_edge_array.splice(0,old_edge_array.length)
  method_node_array.splice(0,old_node_array.length)
  method_node_data.splice(0,old_node_data.length)
  method_edge_array.splice(0,old_edge_array.length)
  active.value = 1
  Node.label = ''
  Node.name = ''
  Node.code = ''
  Node.fullname = ''
  Node.filename = ''
  Node.id = ''

  let url = 'http://localhost:8081/joern/showClassName'
  console.log('submit!')
  //与后端交互
  axios.get(url,{
  params:{
    packetName: form1_update.package,
    }
  })
  .then(response => {
    response.data.forEach((item, index, arr) => {
    classoption.push({
      value: item.fullName,
      label: item.name,
    })
    node_array.push({
      x: Math.random() * 100,
      y: Math.random() * 100,
      id: item.id,
      name: item.name,
      symbolSize: 50,
      draggable: true,
      itemStyle: {
        color: mapcolor.get(item.label)
      },
      label:{
        show:true   //在最后产生的方法上
      },
    })
    node_data.push({
      label: item.label,
      name: item.name,
      code:item.code,
      fullname: item.fullName,
      filename: item.fileName,
      id: item.id,
    })
    })
    var option
    myChart.setOption(
      (option = {
        title: {
          text: ''
        },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'none',
            // progressiveThreshold: 700,
            data: node_array,
            edges: edge_array,
            emphasis: {
              focus: 'adjacency',
              label: {
                position: 'right',
                show: true
              }
            },
            edgeSymbol: ['', 'arrow'],
            roam: true,
            lineStyle: {
              width: 0.5,
              curveness: 0.3,
              opacity: 0.7
            }
          }
        ]
      }),
      true
    );
    myChart.hideLoading()
    })
  .catch(error => {
    console.error(error);
    })
    
    form1_update.isclass = false
}

//返回到上一个步骤，只显示类
const classback = () => {
  var myChart = echarts.init(mycharts.value);
  methodoption.splice(0,methodoption.length)
  myChart.showLoading()
  active.value = 1
  classselected = -1
  edge_array = JSON.parse(JSON.stringify(old_edge_array))
  node_array = JSON.parse(JSON.stringify(old_node_array))
  node_data = JSON.parse(JSON.stringify(old_node_data))
  //将状态还原到只有类时
  Node.label = ''
  Node.name = ''
  Node.code = ''
  Node.fullname = ''
  Node.filename = ''
  Node.id = ''

  old_selectindex = -1
  node_data.forEach((item,index,arr) => {
      node_array[index].itemStyle.color = mapcolor.get(node_data[index].label)
  })

  console.log(old_edge_array)
  var option
    myChart.setOption(
      (option = {
        title: {
          text: ''
        },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'none',
            // progressiveThreshold: 700,
            data: node_array,
            edges: edge_array,
            emphasis: {
              focus: 'adjacency',
              label: {
                position: 'right',
                show: true
              }
            },
            edgeSymbol: ['', 'arrow'],
            roam: true,
            lineStyle: {
              width: 0.5,
              curveness: 0.3,
              opacity: 0.7
            }
          }
        ]
      }),
      true
    );
    myChart.hideLoading()
}

//查找类下的方法

const getMethod = () => {
  //console.log(selectclass.value)
  //需要先确定是否有节点被选中
  if(Node.id == '' || Node.label != 'TYPE_DECL'){
    ElMessageBox.alert('请双击选择需要查询的类节点再确定','提示',{
      confirmButtonText: '确认',
    })
    return
  }

  method_node_array.splice(0,old_node_array.length)
  method_node_data.splice(0,old_node_data.length)
  method_edge_array.splice(0,old_edge_array.length)

  old_edge_array = JSON.parse(JSON.stringify(edge_array))
  old_node_array = JSON.parse(JSON.stringify(node_array))
  old_node_data = JSON.parse(JSON.stringify(node_data))
  var myChart = echarts.init(mycharts.value);
  myChart.showLoading()
  methodoption.splice(0,methodoption.length)
  active.value = 2

  let url = 'http://localhost:8081/joern/showMethodName'
  let isource = Node.id
  console.log('submit!')
  //与后端交互
  axios.get(url,{
  params:{
    className: Node.fullname,
    }
  })
  .then(response => {
    response.data.forEach((item,index,arr) => {
      methodoption.push({
        value: item.fullName,
        label: item.name,
    })

    node_array.push({
      x: Math.random() * 100,
      y: Math.random() * 100,
      id: item.id,
      name: item.name,
      symbolSize: 50,
      draggable: true,
      itemStyle: {
        color: mapcolor.get(item.label)
      },
      label:{
        show:true   //在最后产生的方法上
      },
    })
    node_data.push({
      label: item.label,
      name: item.name,
      code:item.code,
      fullname: item.fullName,
      filename: item.fileName,
      id: item.id,
    })
    
    let itarget = item.id
    var edge = {source:isource,target:itarget}
    if(!edge_array.some(item2 => item2.source == edge.source && item2.target == edge.target)){
      edge_array.push(edge)
    }
    

    })

    var option
    myChart.setOption(
      (option = {
        title: {
          text: ''
        },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'none',
            // progressiveThreshold: 700,
            data: node_array,
            edges: edge_array,
            emphasis: {
              focus: 'adjacency',
              label: {
                position: 'right',
                show: true
              }
            },
            edgeSymbol: ['', 'arrow'],
            roam: true,
            lineStyle: {
              width: 0.5,
              curveness: 0.3,
              opacity: 0.7
            }
          }
        ]
      }),
      true
    );
    myChart.hideLoading()
    classselected = JSON.parse(JSON.stringify(Node))
    console.log(classselected)
    })
  .catch(error => {
    console.error(error);
    })
    form1_update.ismethod = false
    
}

const onSubmit1 = () => {
  /* 测试代码
  PathArray.splice(0,PathArray.length)
  let url = ''
  if(form1.isup == 1){
    url = 'http://localhost:8081/joern/methodUp'
  }
  else{
    url = 'http://localhost:8081/joern/methodDown'
  }
  console.log('submit!')
  //这里填写与后端交互的代码
  axios.get(url,{
  params:{
    className: form1.class,
    methodName: form1.method,
  }
})
.then(response => {
  response.data.forEach(item => {
    PathArray.push(item)
  })
  console.log(PathArray)
  })
  .catch(error => {
    console.error(error);
  })
  */
  PathArray.splice(0,PathArray.length)
  let url = ''
  if(form1.isup == 1){
    url = 'http://localhost:8081/joern/methodUp'
  }
  else{
    url = 'http://localhost:8081/joern/methodDown'
  }
  console.log('submit!')
  //与后端交互
  axios.get(url,{
  params:{
    className: form1.class,
    methodName: form1.method,
    }
  })
  .then(response => {
    response.data.forEach(item => {
    PathArray.push(item)
    
    })
    console.log(PathArray)
    })
  .catch(error => {
    console.error(error);
    })
  
}
//回到上一个步骤，显示类和类的方法
const methodback = () =>  {
  var myChart = echarts.init(mycharts.value);
  methodoption.splice(0,methodoption.length)
  myChart.showLoading()
  active.value = 2
  classselected = -1
  edge_array = JSON.parse(JSON.stringify(method_edge_array))
  node_array = JSON.parse(JSON.stringify(method_node_array))
  node_data = JSON.parse(JSON.stringify(method_node_data))
  //将状态还原到只有类和方法时
  Node.label = ''
  Node.name = ''
  Node.code = ''
  Node.fullname = ''
  Node.filename = ''
  Node.id = ''


  old_selectindex = -1
  node_data.forEach((item,index,arr) => {
      node_array[index].itemStyle.color = mapcolor.get(node_data[index].label)

  })

  console.log(method_edge_array)
  var option
    myChart.setOption(
      (option = {
        title: {
          text: ''
        },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'none',
            // progressiveThreshold: 700,
            data: node_array,
            edges: edge_array,
            emphasis: {
              focus: 'adjacency',
              label: {
                position: 'right',
                show: true
              }
            },
            edgeSymbol: ['', 'arrow'],
            roam: true,
            lineStyle: {
              width: 0.5,
              curveness: 0.3,
              opacity: 0.7
            }
          }
        ]
      }),
      true
    );
    myChart.hideLoading()
}

const onReset1 = () => {
  form1.class = ''
  form1.method = ''
}

const onSubmit1_update = () => {
  form1_update.class = selectclass.value.label
  form1_update.method = selectmethod.value.label
  PathArray.splice(0,PathArray.length)
  if(Node.id == '' || Node.label != 'METHOD'){
    ElMessageBox.alert('请双击选择需要查询的方法节点再确定','提示',{
      confirmButtonText: '确认',
    })
    return
  }
  method_edge_array = JSON.parse(JSON.stringify(edge_array))
  method_node_array = JSON.parse(JSON.stringify(node_array))
  method_node_data = JSON.parse(JSON.stringify(node_data))

  var myChart = echarts.init(mycharts.value);
  myChart.showLoading()
  active.value = 3

  let url = 'http://localhost:8081/joern/showInvocationLink'
  console.log('submit!')
  //与后端交互
  axios.get(url,{
  params:{
    className: classselected.name,
    methodName: Node.name,
    isDown: form1_update.isdown.toString(),
    }
  })
  .then(response => {
    response.data.forEach((item_p, index_p, arr_p) => {
      item_p.pathMember.forEach((item, index, arr) => {
      if(!node_data.some(item2 => item2.id == item.id)){
      
      node_array.push({
        x: 150 + Math.random() * 100,
        y: 150 + Math.random() * 100,
        id: item.id,
        name: item.name,
        symbolSize: 50,
        draggable: true,
        itemStyle: {
          color: mapcolor.get(item.label)
        },
        label:{
          show:true   //在最后产生的方法上
        },
      })
      node_data.push({
        label: item.label,
        name: item.name,
        code:item.code,
        fullname: item.fullName,
        filename: item.fileName,
        id: item.id,
      })
    }
      if(index + 1 < arr.length){
        let isource = item.id
        let itarget = arr[index+1].id
        var edge = {source:isource,target:itarget}
        if(!edge_array.some(item2 => item2.source == edge.source && item2.target == edge.target)){
          edge_array.push(edge)
        }
      }

      })
    })
    var option
    myChart.setOption(
      (option = {
        title: {
          text: ''
        },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'none',
            // progressiveThreshold: 700,
            data: node_array,
            edges: edge_array,
            emphasis: {
              focus: 'adjacency',
              label: {
                position: 'right',
                show: true
              }
            },
            edgeSymbol: ['', 'arrow'],
            roam: true,
            lineStyle: {
              width: 0.5,
              curveness: 0.3,
              opacity: 0.7
            }
          }
        ]
      }),
      true
    );
      console.log(response.data)
      myChart.hideLoading()
    })
  .catch(error => {
    console.error(error);
    })
  
}

const onReset1_update = () => {
  form1_update.package = ''
  form1_update.isdown= false
  form1_update.isclass= true
  form1_update.ismethod= true
  classselected = null
  selectclass.value = ''
  selectmethod.value = ''
  radiodisabled.value = false
  Node.label = ''
  Node.name = ''
  Node.code = ''
  Node.fullname = ''
  Node.filename = ''
  Node.id = ''
  node_array = []
  edge_array = []
  node_data = []
  old_node_array = []
  old_edge_array = []
  old_node_data = []
  active.value = 0
  old_selectindex = -1 
  var myChart = echarts.init(mycharts.value);
  myChart.clear()
}

const onSubmit2 = () => {
  //PathArray.splice(0,PathArray.length)
  if(form2.url == ''){
    ElMessageBox.alert('请输入请求路径','提示',{
      confirmButtonText: '确认',
    })
    return
  }

  Node.label = ''
  Node.name = ''
  Node.code = ''
  Node.fullname = ''
  Node.filename = ''
  Node.id = ''
  node_data.splice(0,node_data.length)
  node_array.splice(0,node_array.length)
  edge_array.splice(0,edge_array.length)
  var myChart = echarts.init(mycharts.value);
  myChart.showLoading()
  let url = 'http://localhost:8081/joern/urlPath'
  console.log('submit!')
  //与后端交互
  //url形式为/*/*/*
  axios.get(url,{
  params:{
    url: form2.url,
    }
  })
  .then(response => {
    response.data.forEach((p_item, p_index, p_arr) => {
      p_item.pathMember.forEach((item, index, arr) => {
        if(!node_data.some(item2 => item2.id == item.id)){
        
        node_array.push({
          x: 150 + Math.random() * 100,
          y: 150 + Math.random() * 100,
          id: item.id,
          name: item.name,
          symbolSize: 50,
          draggable: true,
          itemStyle: {
            color: mapcolor.get(item.label)
          },
          label:{
            show:true   //在最后产生的方法上
          },
        })
        node_data.push({
          label: item.label,
          name: item.name,
          code:item.code,
          fullname: item.fullName,
          filename: item.fileName,
          id: item.id,
        })
      }
        if(index + 1 < arr.length){
          let isource = item.id
          let itarget = arr[index+1].id
          var edge = {source:isource,target:itarget}
          if(!edge_array.some(item2 => item2.source == edge.source && item2.target == edge.target)){
            edge_array.push(edge)
          }
        }
      })
    
    })
    var option
    myChart.setOption(
      (option = {
        title: {
          text: ''
        },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'none',
            // progressiveThreshold: 700,
            data: node_array,
            edges: edge_array,
            emphasis: {
              focus: 'adjacency',
              label: {
                position: 'right',
                show: true
              }
            },
            edgeSymbol: ['', 'arrow'],
            roam: true,
            lineStyle: {
              width: 0.5,
              curveness: 0.3,
              opacity: 0.7
            }
          }
        ]
      }),
      true
    );

    myChart.hideLoading()

    })
  .catch(error => {
    console.error(error);
    })
  
}

const onReset2 = () => {
  form2.url = ''
  Node.label = ''
  Node.name = ''
  Node.code = ''
  Node.fullname = ''
  Node.filename = ''
  Node.id = ''
  node_data.splice(0,node_data.length)
  node_array.splice(0,node_array.length)
  edge_array.splice(0,edge_array.length)
  var myChart = echarts.init(mycharts.value);
  myChart.clear()
}

const onSubmit3 = () => {
  //PathArray.splice(0,PathArray.length)

  if(form3.table == ''){
    ElMessageBox.alert('请输入表名','提示',{
      confirmButtonText: '确认',
    })
    return
  }

  if(form3.field == ''){
    ElMessageBox.alert('请输入字段名','提示',{
      confirmButtonText: '确认',
    })
    return
  }

  Node.label = ''
  Node.name = ''
  Node.code = ''
  Node.fullname = ''
  Node.filename = ''
  Node.id = ''
  node_data.splice(0,node_data.length)
  node_array.splice(0,node_array.length)
  edge_array.splice(0,edge_array.length)
  var myChart = echarts.init(mycharts.value);
  myChart.showLoading()

  let url = 'http://localhost:8081/joern/dataBaseInfo'
  console.log('submit!')
  //与后端交互
  //url形式为/*/*/*
  axios.get(url,{
  params:{
    dataBaseName: form3.database,
    tableName: form3.table,
    fieldName: form3.field,
    }
  })
  .then(response => {
    response.data.forEach((p_item, p_index, p_arr) => {
      p_item.pathMember.forEach((item, index, arr) => {
        if(!node_data.some(item2 => item2.id == item.id)){
        console.log(item,item.label)
        node_array.push({
          x: 150 + Math.random() * 100,
          y: 150 + Math.random() * 100,
          id: item.id,
          name: item.name,
          symbolSize: 50,
          draggable: true,
          itemStyle: {
            color: mapcolor.get(item.label)
          },
          label:{
            show:true   //在最后产生的方法上
          },
        })
        node_data.push({
          label: item.label,
          name: item.name,
          code:item.code,
          fullname: item.fullName,
          filename: item.fileName,
          id: item.id,
        })
      }
        if(index + 1 < arr.length){
          let isource = item.id
          let itarget = arr[index+1].id
          var edge = {source:isource,target:itarget}
          if(!edge_array.some(item2 => item2.source == edge.source && item2.target == edge.target)){
            edge_array.push(edge)
          }
        }
      })
    
    })
    var option
    myChart.setOption(
      (option = {
        title: {
          text: ''
        },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'none',
            // progressiveThreshold: 700,
            data: node_array,
            edges: edge_array,
            emphasis: {
              focus: 'adjacency',
              label: {
                position: 'right',
                show: true
              }
            },
            edgeSymbol: ['', 'arrow'],
            roam: true,
            lineStyle: {
              width: 0.5,
              curveness: 0.3,
              opacity: 0.7
            }
          }
        ]
      }),
      true
    );

    myChart.hideLoading()

    
    console.log(PathArray)
    })
  .catch(error => {
    console.error(error);
    })
}

const onReset3 = () => {
  form3.database = ''
  form3.field = ''
  form3.table = ''
  Node.label = ''
  Node.name = ''
  Node.code = ''
  Node.fullname = ''
  Node.filename = ''
  Node.id = ''
  node_data.splice(0,node_data.length)
  node_array.splice(0,node_array.length)
  edge_array.splice(0,edge_array.length)
  var myChart = echarts.init(mycharts.value);
  myChart.clear()
}

//点击节点显示属性

const handleclick = (Pathindex, Nodeindex) =>{
  Node.name = PathArray[Pathindex].pathMember[Nodeindex].name
  Node.label = PathArray[Pathindex].pathMember[Nodeindex].label
  Node.code = PathArray[Pathindex].pathMember[Nodeindex].code
  Node.fullname = PathArray[Pathindex].pathMember[Nodeindex].fullName
  Node.filename = PathArray[Pathindex].pathMember[Nodeindex].fileName
}


//定义提交和重置的函数

</script>

<template>
  <el-row style="height:31.1px;width:100%">
    <el-col style="width:100%" :span="24"><div class="head">代码血缘分析工具DEMO</div></el-col>
  </el-row>
  <el-row>
  <el-col :span="3">
    <el-row>
      <el-col :span="24">
        <el-menu
          default-active="1-1"
          background-color="#0a0a0a"
          text-color="white"
          active-text-color="#DDBEF6"
          style="width:240px;height:100vh"
          @select="handleSelect"
        >
          <el-sub-menu index="1">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>数据库分析</span>
            </template>
            <el-menu-item-group >
              <el-menu-item index="1-1">根据类名查找</el-menu-item>
              <el-menu-item index="1-2">根据url查找</el-menu-item>
              <el-menu-item index="1-3">根据表名查找</el-menu-item>
            </el-menu-item-group>
          </el-sub-menu>
          <el-menu-item index="2">
            <el-icon><Upload /></el-icon>
            <span>分析文件上传下载</span>
          </el-menu-item>
        </el-menu>

      </el-col>
    </el-row>
  </el-col>
  <el-col :span="21">
  <el-row>
    <el-col :span="14">

        <el-card style="max-width: 900px; margin-left:5%">
          <template #header>
            <div class="card-header">
                <div class="chaindisplayhead">调用链</div>
            </div>
          </template>
          <!--调用链显示框-->
          <div>
              <!--
              里面放button
              这里用for循环来遍历渲染获得的节点所有属性

              <el-row v-for="(Pathitem,Pathindex) in PathArray" style="white-space: nowrap">
                <el-col :span="24" >
                  <span v-for="(Nodeitem,Nodeindex) in Pathitem.pathMember">
                    <el-tooltip class="box-item" effect="dark" :content="Nodeitem.name" placement="top-start">
                      <el-button type="primary" circle @click="handleclick(Pathindex,Nodeindex)" style="width: 70px;height: 70px;padding:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{Nodeitem.name}}</el-button>
                    </el-tooltip>
                    <el-icon v-if="Nodeindex < Pathitem.pathMember.length - 1" style="positive:relative;top:4px" color="#409EFF"><DArrowRight /></el-icon>
                  </span>

                </el-col>
              </el-row>
            -->
            <div ref="mycharts" style="width:100%;height:500px">
            </div>
          </div>
        </el-card>
      </el-col>
    <el-col :span="10">
      <el-card style="max-width: 400px; margin-left: 5%;">
        <template #header>
          <div class="card-header">
              <div class="propertydisplayhead">节点属性</div>
          </div>
        </template>
        <!--属性显示框-->
        <div>
          <el-scrollbar style="white-space: normal;" height="300px">
            <!--里面放描述列表-->
            <el-row >
              <el-descriptions direction="vertical" column="1">
                <el-descriptions-item width="100px" v-model="Node.label" label="节点标签">
                  <el-tag v-if="Node.label != ''" size="small">{{Node.label}}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item v-model="Node.id" label="节点ID">{{Node.id}}</el-descriptions-item>
                <el-descriptions-item v-model="Node.name" label="节点名">{{Node.name}}</el-descriptions-item>
                <el-descriptions-item v-model="Node.code" label="代码">{{Node.code}}</el-descriptions-item>
                <el-descriptions-item v-model="Node.fullname" label="节点全名">{{Node.fullname}}</el-descriptions-item>
                <el-descriptions-item v-model="Node.filename" label="节点文件名">{{Node.filename }}</el-descriptions-item>

              </el-descriptions>
            </el-row>
          </el-scrollbar>
        </div>
      </el-card>
      <div>
        <el-row v-if="selectvalue == 'Object1_update'">
          <!--Object1_update的情况-->
          <!--用表单-->
          <el-form model="form1_update" label-width="auto" style="max-width: 300px; margin-left:5%;margin-top:5%">
            <el-form-item style="width: 600px;" label="请输入要查找的包">
              <el-row>
                <el-col :span="21">
                  <el-input style="width:200px" v-model="form1_update.package" ></el-input>
                </el-col>
                <el-col :span="3">
                  <el-button @click="getClass">
                    查找
                  </el-button>
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item style="width: 450px;" label="请选择调用方向">
              <el-radio-group v-model="form1_update.isdown" class="ml-4">
                <el-radio :value="false" :disabled = radiodisabled>向上调用</el-radio>
                <el-radio :value="true"  :disabled = radiodisabled>向下调用</el-radio>
              </el-radio-group>
              <el-button style="margin-left:40px" @click="onReset1_update">
                重置
              </el-button>
            </el-form-item>
      
      
          </el-form>
          
        </el-row>
        <el-row v-else-if="selectvalue == 'Object2'">
          <!--Object2的情况-->
          <!--用表单-->
          <el-form model="form2" label-width="auto" style="max-width: 300px; margin-left:5%;margin-top:5%">
            <el-form-item style="width: 450px;" label="请输入请求路径">
              <el-input style="width:275px" v-model="form2.url" ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onSubmit2">
                提交
              </el-button>
              <el-button @click="onReset2">
                重置
              </el-button>
            </el-form-item>
      
      
          </el-form>
      
        </el-row>
        <el-row v-if="selectvalue == 'Object3'">
          <!--Object3的情况-->
          <!--用表单-->
          <el-form model="form3" label-width="auto" style="max-width: 300px; margin-left:5%;margin-top:5%">
            <!--
            <el-form-item style="width: 450px;" label="请输入数据库名">
              <el-input v-model="form3.database" ></el-input>
            </el-form-item>
            -->
            <el-form-item style="width: 450px;" label="请输入数据库表名">
              <el-input style="width:275px" v-model="form3.table" ></el-input>
            </el-form-item>
            <el-form-item style="width: 450px;" label="请输入字段名">
              <el-input style="width:275px" v-model="form3.field" ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onSubmit3">
                提交
              </el-button>
              <el-button @click="onReset3">
                重置
              </el-button>
            </el-form-item>
      
      
          </el-form>
        </el-row>
        <el-row v-else> <!--这里先置为空，不知道还有用没-->
        </el-row>
      </div>
    </el-col>
  </el-row>
  <el-row v-if="selectvalue == 'Object1_update'"> 
    <el-col :span="24">
    <el-steps :active="active" align-center finish-status="success" style="width:33%;margin-left:12%">
      <el-step title="第一步" description="输入包和调用方向"/>
      <el-step title="第二步" description="选择要查询的类"/>
      <el-step title="第三步" description="选择要查询的方法"/>
    </el-steps>
    <div v-if="active == 1"> <!--第二步是确定类并查找类-->
    <div>
    <el-button type="success" style="margin-left:25.8%" @click="getMethod">确定查找</el-button>
    </div>
    <div>
    <el-button style="margin-left:26.6%;margin-top:5px" @click="onReset1_update">返回</el-button>
    </div>
    </div>
    <div v-if="active == 2"> <!--第二步是确定类并查找类-->
      <div>
      <el-button type="success" style="margin-left:36.5%" @click="onSubmit1_update">确定查找</el-button>
      </div>
      <div>
      <el-button style="margin-left:37.3%;margin-top:5px" @click="classback">返回</el-button>
      </div>
    </div>
    <div v-if="active == 3"> <!--第二步是确定类并查找类-->
      <div>
      <el-button style="margin-left:37.3%;margin-top:5px" @click="methodback">返回</el-button>
      </div>
    </div>
    </el-col>

    <!--新的一行放选择器-->
    <!--
    <el-col style="max-width: 400px; margin-left: 12%;" :span="24">
      <el-select
      v-model="selectvalue"
      placeholder="Select"
      size="large"
      style="width: 240px"
    >
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>

    </el-col>
  -->
  </el-row>

  <!--这里要分开渲染-->
  <!--
  <el-row v-if="selectvalue == 'Object1'">
    Object1的情况
    用表单
    <el-form model="form1" label-width="auto" style="max-width: 300px; margin-left:12%">
      <el-form-item style="width: 450px;" label="请输入要查找的类">
        <el-input v-model="form1.class" ></el-input>
      </el-form-item>
      <el-form-item style="width: 450px;" label="请输入要查找的方法">
        <el-input v-model="form1.method" ></el-input>
      </el-form-item>
      <el-form-item style="width: 450px;" label="请选择调用方向">
        <el-radio-group v-model="form1.isup" class="ml-4">
          <el-radio :value="1"  >向上调用</el-radio>
          <el-radio :value="0"  >向下调用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item style="margin-left:50%;">
        <el-button type="primary" @click="onSubmit1">
          提交
        </el-button>
        <el-button @click="onReset1">
          重置
        </el-button>
      </el-form-item>


    </el-form>

  </el-row>
  -->
<!--旧版布局，先搁置在这里
  <el-row v-if="selectvalue == 'Object1_update'">
    <el-form model="form1_update" label-width="auto" style="max-width: 300px; margin-left:12%">
      <el-form-item style="width: 600px;" label="请输入要查找的包">
        <el-row>
          <el-col :span="21">
            <el-input style="width:310px" v-model="form1_update.package" ></el-input>
          </el-col>
          <el-col :span="3">
            <el-button @click="getClass">
              查找
            </el-button>
          </el-col>
        </el-row>
      </el-form-item>
      <el-form-item style="width: 600px;" label="请选择要查找的类">
        <el-row v-model="form1_update.isclass">
          <el-col :span="21">
            <el-select
            :disabled= form1_update.isclass
            v-model="selectclass"
            placeholder="Select"
            size="medium"
            style="width: 310px"
            >
            <el-option
              v-for="item in classoption"
              :key="item.value"
              :label="item.label"
              :value="item"
            />
          </el-select>
          </el-col>
          <el-col :span="3">
            <el-button @click="getMethod">
              查找
            </el-button>
          </el-col>
        </el-row>
      </el-form-item>
      <el-form-item style="width: 450px;" label="请选择要查找的方法">
        <el-row v-model="form1_update.ismethod">
          <el-col :span="24">
            <el-select 
            v-model="selectmethod"
            :disabled= form1_update.ismethod
            placeholder="Select"
            size="medium"
            style="width: 310px"
            >
            <el-option v-model="form1_update.class"
              v-for="item in methodoption"
              :key="item.value"
              :label="item.label"
              :value="item"
            />
          </el-select>
          </el-col>
        </el-row>
      </el-form-item>
      <el-form-item style="width: 450px;" label="请选择调用方向">
        <el-radio-group v-model="form1_update.isdown" class="ml-4">
          <el-radio :value="false"  >向上调用</el-radio>
          <el-radio :value="true"  >向下调用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item style="margin-left:50%;">
        <el-button type="primary" @click="onSubmit1_update">
          提交
        </el-button>
        <el-button @click="onReset1_update">
          重置
        </el-button>
      </el-form-item>


    </el-form>

  </el-row>
-->
</el-col>
</el-row>

</template>

<style lang="scss">
  .el-row {
    margin-bottom: 20px;
  }
  .el-row:last-child {
    margin-bottom: 0;
  }
  .el-col {
    border-radius: 4px;
  }

  .head {
    background-color: #1d1e1f; /* 深蓝色背景 */
    color: #ffffff;           /* 白色字体 */
    padding: 10px;            /* 内边距 */
    text-align: center;       /* 居中对齐 */
    font-size: 24px;          /* 字体大小 */
    font-family: Arial, sans-serif; /* 字体 */
  }


  .chaindisplayhead {
    background-color: #305a88; 
    border-radius: 5px;
    color: #ffffff;           /* 白色字体 */
    padding: 10px;            /* 内边距 */
    text-align: center;       /* 居中对齐 */
    font-size: 16px;          /* 字体大小 */
    font-family: Arial, sans-serif; /* 字体 */
    font-weight: bold;
  }

  .propertydisplayhead {
    background-color: #305a88; 
    border-radius: 5px;
    color: #ffffff;           /* 白色字体 */
    padding: 10px;            /* 内边距 */
    text-align: center;       /* 居中对齐 */
    font-size: 16px;          /* 字体大小 */
    font-family: Arial, sans-serif; /* 字体 */
    font-weight: bold;
  }

  .grid-content {
    border-radius: 4px;
    min-height: 36px;
  }
  
  .el-menu-vertical-demo:not(.el-menu--collapse) {
    width: 180px;
    min-height: 400px;
  }

  .row-bg {
    padding: 10px 0;
    background-color: var(--ep-c-bg-row);
  }
  
  .ep-bg-purple-dark {
    background: var(--ep-c-bg-purple-dark);
  }
  
  .ep-bg-purple {
    background: var(--ep-c-bg-purple);
  }
  
  .ep-bg-purple-light {
    background: var(--ep-c-bg-purple-light);
  }

	html,
	body,
	#app {//这里设置强制铺满全屏
		margin: 0;
		padding: 0;
		height: 100%;
	}

</style>
