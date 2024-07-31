package com.icbc.codeResolver.service;

import com.icbc.codeResolver.config.AsyncThreadPoolConfig;
import com.icbc.codeResolver.entity.FileDto;
import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.entity.AsyncTaskProgress;
import com.icbc.codeResolver.utils.StreamGobbler;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@DubboService(group = "parse",timeout = 100000000)//设置超时时间
@RefreshScope
public class JoernParseServiceImpl implements JoernParseService {
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


    private static final String REDIS_PREFIX = "ASYNC_TASK_"; // Redis中保存异步任务进度对象的前缀
    @Autowired
    public RedisTemplate redisTemplate;
    //生成异步任务的id，用于前端查询改任务的进度使用


    /**
     * 异步解析业务
     * @param url
     * @param taskId
     * @throws IOException
     */
    @Override
    public void AsyncParse(String url,String taskId) throws IOException {
        CompletableFuture.runAsync(()->{
            //异步任务进度条
            AsyncTaskProgress taskProgress=new AsyncTaskProgress();
            int total = 0;
            taskProgress.setStatus("PENDING"); //任务状态：进行中
            taskProgress.setProgress(total);//任务进度: 0
            taskProgress.setResult(null); //任务结果：空
            taskProgress.setError(null); //任务错误：空
            //核心异步业务
            //删除原来数据库文件夹下的旧csv文件
            File directory = new File("E:\\software\\Neo4j\\Data\\relate-data\\dbmss\\dbms-dc752007-1b88-4751-adae-6f161a7539d7\\import");
            try {
                FileUtils.cleanDirectory(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("url为"+url);
            List<String> commands=new ArrayList<>();
            int i=0;
            System.setProperty("com/icbc/codeResolver",joern_dir);
            System.setProperty("import_db",toDb_dir);
            commands.add("rmdir " + joern_dir + "\\out" );//这里需要先把out删了再后面创建
            commands.add("joern-parse "+url+" -o "+cpg_dir);
            commands.add("joern-export.bat --repr=all --format=neo4jcsv "+cpg_dir);
            commands.add("move "+source_dir+" "+import_dir);
            commands.add("for %f in ("+node_dir+") do bin\\cypher-shell -u neo4j -p 19990528 -d mvc --file %f");
            commands.add("for %f in ("+edge_dir+") do bin\\cypher-shell -u neo4j -p 19990528 -d mvc --file %f");
            try{
                for (String command : commands) {
                    total++;
                    int percent = (int) (total * 100.0 / command.length());
                    ProcessBuilder builder = new ProcessBuilder();
                    builder.command("cmd.exe", "/c",command);
                    builder.directory(i++<4?new File(System.getProperty("joern")):new File(System.getProperty("import_db")));
                    Process process=builder.start();
                    StreamGobbler streamGobbler =
                            new StreamGobbler(process.getInputStream(), System.out::println);
                    Executors.newSingleThreadExecutor().submit(streamGobbler);
                    int exitCode = process.waitFor();
                    taskProgress.setProgress(percent);
                    assert exitCode == 0;
                }
                taskProgress.setProgress(100);
                taskProgress.setResult("Task result"); // 任务结果
                taskProgress.setStatus("SUCCESS"); // 任务状态：成功
                saveAsyncTaskProgress(taskId,taskProgress);
            }catch (Exception e){
                taskProgress.setError(e.getMessage());
                taskProgress.setStatus("FAILURE");
                saveAsyncTaskProgress(taskId,taskProgress);
            }
        }, AsyncThreadPoolConfig.getExecutor());
    }
    //保存进度条进度
    private void saveAsyncTaskProgress(String taskId,AsyncTaskProgress taskProgress){
        redisTemplate.opsForValue().set(REDIS_PREFIX + taskId, taskProgress, 30, TimeUnit.MINUTES); // 将任务进度对象保存到Redis中，有效期30分钟
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
        commands.add("for %f in ("+node_dir+") do bin\\cypher-shell -u neo4j -p 19990528 -d mvc --file %f");
        commands.add("for %f in ("+edge_dir+") do bin\\cypher-shell -u neo4j -p 19990528 -d mvc --file %f");
        for (String command : commands) {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("cmd.exe", "/c",command);
            builder.directory(i++<4?new File(System.getProperty("joern")):new File(System.getProperty("import_db")));
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
        return Result.successful("解析成功");
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
