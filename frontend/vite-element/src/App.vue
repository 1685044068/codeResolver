<script setup>
import axios from 'axios';


//测试代码
import { reactive, ref, onMounted } from 'vue'
//定义选择目标的绑定变量
const selectvalue = ref('')

//定义表单输入的东西
const form1 = reactive({
  class: '',
  method: '',
  isup: 1,
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
    value: 'Object2',
    label: '目标2',
  },
  {
    value: 'Object3',
    label: '目标3',
  },
]

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
})

const PathArray = reactive([])

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

const onReset1 = () => {
  form1.class = ''
  form1.method = ''
}

const onSubmit2 = () => {
  PathArray.splice(0,PathArray.length)
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
    response.data.forEach(item => {
    PathArray.push(item)
    
    })
    console.log(PathArray)
    })
  .catch(error => {
    console.error(error);
    })
  
}

const onReset2 = () => {
  form2.url = ''

}

const onSubmit3 = () => {
  PathArray.splice(0,PathArray.length)
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
    response.data.forEach(item => {
    PathArray.push(item)
    
    })
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
  <el-row>
    <el-col :span="24"><div class="head">代码血缘分析工具DEMO</div></el-col>
  </el-row>
  <el-row>
    <el-col :span="14">
        <el-card style="max-width: 700px; margin-left:20%">
          <template #header>
            <div class="card-header">
                <div class="chaindisplayhead">调用链</div>
            </div>
          </template>
          <!--调用链显示框-->
          <div>
            <el-scrollbar height="300px">
              <!--里面放button-->
                <!--这里用for循环来遍历渲染获得的节点所有属性-->

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
            </el-scrollbar>
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
                <el-descriptions-item v-model="Node.name" label="节点名">{{Node.name}}</el-descriptions-item>
                <el-descriptions-item v-model="Node.code" label="代码">{{Node.code}}</el-descriptions-item>
                <el-descriptions-item v-model="Node.fullname" label="节点全名">{{Node.fullname}}</el-descriptions-item>
                <el-descriptions-item v-model="Node.filename" label="节点文件名">{{Node.filename }}</el-descriptions-item>

              </el-descriptions>
            </el-row>
          </el-scrollbar>
        </div>
      </el-card>
    </el-col>
  </el-row>
  <el-row>
    <!--新的一行放选择器-->
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
  </el-row>
  <!--这里要分开渲染-->
  <el-row v-if="selectvalue == 'Object1'">
    <!--Object1的情况-->
    <!--用表单-->
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
  <el-row v-else-if="selectvalue == 'Object2'">
    <!--Object2的情况-->
    <!--用表单-->
    <el-form model="form2" label-width="auto" style="max-width: 300px; margin-left:12%">
      <el-form-item style="width: 450px;" label="请输入请求路径">
        <el-input v-model="form2.url" ></el-input>
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
  <el-row v-else-if="selectvalue == 'Object3'">
    <!--Object3的情况-->
    <!--Object2的情况-->
    <!--用表单-->
    <el-form model="form3" label-width="auto" style="max-width: 300px; margin-left:12%">
      <!--
      <el-form-item style="width: 450px;" label="请输入数据库名">
        <el-input v-model="form3.database" ></el-input>
      </el-form-item>
      -->
      <el-form-item style="width: 450px;" label="请输入数据库表名">
        <el-input v-model="form3.table" ></el-input>
      </el-form-item>
      <el-form-item style="width: 450px;" label="请输入字段名">
        <el-input v-model="form3.field" ></el-input>
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
  <el-row v-else>
    <!--Object2的情况-->
    <el-text class="mx-1" style="font-size:large; margin-left:12%;" type="info">请选择目标</el-text>
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
    background-color: #2c3e50; /* 深蓝色背景 */
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

  :root {
    --ep-c-bg-row: #f9fafc;
    --ep-c-bg-purple: #d3dce6;
    --ep-c-bg-purple-dark: #99a9bf;
    --ep-c-bg-purple-light: #e5e9f2;
  }
  
  .dark {
    --ep-c-bg-row: #18191a;
    --ep-c-bg-purple: #46494d;
    --ep-c-bg-purple-dark: #242526;
    --ep-c-bg-purple-light: #667180;
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

</style>
