package com.icbc.codeResolver.service;

import com.icbc.codeResolver.config.AsyncThreadPoolConfig;
import com.icbc.codeResolver.entity.AsyncTaskProgress;
import com.icbc.codeResolver.entity.FileDto;
import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.utils.StreamGobbler;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@DubboService(group = "parse",timeout = 100000000)//设置超时时间
@RefreshScope
public class JoernParseServiceImpl implements JoernParseService {
    private static Logger logger = Logger.getLogger(JoernParseServiceImpl.class);
    @Value("${import_to_db.dir}")
    private String toDb_dir;
    @Value("${joern.dir}")
    private String joern_dir;
    @Value("${import.dir}")
    private String import_dir;
    @Value("${source.dir}")
    private String source_dir;
    @Value("${node_csv}")
    private String node_dir;
    @Value("${edge_csv}")
    private String edge_dir;
    @Value("${cpg.dir}")
    private String cpg_dir;
    @Value("${database}")
    private String database;
    @Value("${upload.dir}")
    private String upload_dir;

    @Autowired
    ThreadPoolTaskExecutor taskExecutor;
    private static final String REDIS_PREFIX = "ASYNC_TASK_"; // Redis中保存异步任务进度对象的前缀
    @Autowired
    public RedisTemplate redisTemplate;
    /**
     * 解析业务
     * @param url
     * @return
     */
    @Override
    public Result parse(String url) throws IOException {
        //删除原来数据库文件夹下的旧csv文件
        File directory = new File("E:\\software\\Neo4j\\Data\\relate-data\\dbmss\\dbms-dc752007-1b88-4751-adae-6f161a7539d7\\import");
        FileUtils.cleanDirectory(directory);

        System.out.println("url为"+url);
        List<String> commands=new ArrayList<>();
        int i=0;
        System.setProperty("com/icbc/codeResolver",joern_dir);
        System.setProperty("import_db",toDb_dir);

        commands.add("rmdir " + joern_dir + "\\out" );//这里需要先把out删了再后面创建
        commands.add("joern-parse "+url+" -o "+cpg_dir);
        commands.add("joern-export.bat --repr=all --format=neo4jcsv "+cpg_dir);
        commands.add("move "+source_dir+" "+import_dir);
        commands.add("for %f in ("+node_dir+") do bin\\cypher-shell -u neo4j -p 12345678 -d "+database+" --file %f");
        commands.add("for %f in ("+edge_dir+") do bin\\cypher-shell -u neo4j -p 12345678 -d "+database+" --file %f");
        logger.info("开始导入");
        for (String command : commands) {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("cmd.exe", "/c",command);
            builder.directory(i++<3?new File(System.getProperty("com/icbc/codeResolver")):new File(System.getProperty("import_db")));
            try{
                Process process=builder.start();
                StreamGobbler streamGobbler =
                        new StreamGobbler(process.getInputStream(), System.out::println);
                Executors.newSingleThreadExecutor().submit(streamGobbler);
                int exitCode = process.waitFor();
                assert exitCode == 0;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        logger.info("解析成功！");
        return Result.successful("解析成功");
    }


    /**
     * 异步导入
     * @param url
     * @param taskId
     * @throws IOException
     */
    @Override
    public void AsyncParse(String url, String taskId) throws IOException {
        //清空redis原来的进度数据
        redisTemplate.delete(REDIS_PREFIX + taskId);
        AtomicReference<AsyncTaskProgress> taskProgress= new AtomicReference<>(new AsyncTaskProgress());
        AtomicReference<Integer> success= new AtomicReference<>(0);
        File directory = new File("E:\\software\\Neo4j\\Data\\relate-data\\dbmss\\dbms-dc752007-1b88-4751-adae-6f161a7539d7\\import");
        try {
            org.apache.tomcat.util.http.fileupload.FileUtils.cleanDirectory(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("url为"+url);
        List<String> commands=new ArrayList<>();
        System.setProperty("joern",joern_dir);
        System.setProperty("import_db",toDb_dir);

        commands.add("rmdir " + joern_dir + "\\out" );//这里需要先把out删了再后面创建
        commands.add("joern-parse "+url+" -o "+cpg_dir);
        commands.add("joern-export.bat --repr=all --format=neo4jcsv "+cpg_dir);//joern-export.bat --repr=all --format=neo4jcsv E:\software\Joern\joern\workspace\hmdp\cpg.bin
        commands.add("move "+source_dir+" "+import_dir);
        //执行前4条命令
        try{
            //前三条命令执行
            for (String command : commands){
                logger.info(command);
                String result=doCommand("joern",command);
                logger.info(result);
            }
        }catch (Exception e){

        }
        //导入数据库
        //获取所有文件路径
        File file = new File(import_dir);
        //用数组把文件夹下的文件存起来
        File[] files = file.listFiles();
        logger.info("开始导入");
        for (File file1:files){
            String command="bin\\cypher-shell -u neo4j -p 12345678 -d "+database+" --file "+file1;
            logger.info(command);
            CompletableFuture<Void> taskFuture = CompletableFuture.runAsync(()->{
                String res=doCommand("import_db",command);
                if (res.equals("success")){
                    success.getAndSet(success.get() + 1);

                    taskProgress.set(SetAndSaveProgress(taskProgress.get(), taskId, success,files.length));
                }
                logger.info("成功数为"+success);
                logger.info(command+res);
            }, AsyncThreadPoolConfig.getExecutor());
        }

    }


    /**
     * 设置taskProgress并存入数据库
     * @param taskProgress
     */
    private synchronized AsyncTaskProgress SetAndSaveProgress(AsyncTaskProgress taskProgress,String taskId,
                                                              AtomicReference<Integer> percent,Integer len){
        logger.info("存入值（进度条）为"+percent.get()*100/len);
        taskProgress.setProgress(percent.get()*100/len);
        taskProgress.setStatus("wait");
        if(taskProgress.getProgress()==100){
            taskProgress.setResult("Task result"); // 任务结果
            taskProgress.setStatus("SUCCESS"); // 任务状态：成功
            logger.info("解析成功！");
        }
        redisTemplate.opsForValue().set(REDIS_PREFIX + taskId, taskProgress, 3000, TimeUnit.MINUTES); // 将任务进度对象保存到
        return taskProgress;
    }


    //查询进度
    public AsyncTaskProgress getAsyncTaskProgress(String taskId){
        AsyncTaskProgress taskProgress = (AsyncTaskProgress) redisTemplate.opsForValue().get(REDIS_PREFIX + taskId); // 获取任务进度对象
        if (taskProgress == null) {
            throw new RuntimeException("无法获取异步任务进度，可能已经过期或不存在！"); // 如果获取不到，抛出异常
        }
        return taskProgress;
    }



    /**
     * cmd命令执行
     * @param dir
     * @param command
     * @return
     */
    public String doCommand(String dir,String command){
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("cmd.exe", "/c",command);
        builder.directory(new File(System.getProperty(dir)));
        try{
            Process process=builder.start();
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = process.waitFor();
            assert exitCode == 0;
        }catch (Exception e){
            e.printStackTrace();
            return "fail";
        }
        return "success";
    }



    /**
     * 获取存储路径下的所有文件列表
     * @return
     */
    @Override
    public Result getFileList() {
        //表示一个文件路径
        File file = new File(upload_dir);
        //用数组把文件夹下的文件存起来
        File[] files = file.listFiles();
        List<FileDto> fileDtoList=new ArrayList<>();
        for (File file1:files){
            String end=file1.getName().substring(file1.getName().length()-3);
            if (!end.equals("jar")) continue;
            fileDtoList.add(new FileDto(file1.getName(),file1.getPath()));
        }
        return Result.successful(fileDtoList);
    }
}
