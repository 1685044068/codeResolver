package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.FileDto;
import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.utils.StreamGobbler;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Service
@DubboService(group = "parse",timeout = 100000000)//设置超时时间
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

    /**
     * 解析业务
     * @param url
     * @return
     */
    @Override
    public Result parse(String url){
        System.out.println("url为"+url);
        List<String> commands=new ArrayList<>();
        int i=0;
        System.setProperty("com/icbc/codeResolver",joern_dir);
        System.setProperty("import_db",toDb_dir);
        commands.add("joern-parse "+url+" -o "+cpg_dir);
        commands.add("joern-export.bat --repr=all --format=neo4jcsv "+cpg_dir);
        commands.add("move "+source_dir+" "+import_dir);
        commands.add("for %f in ("+node_dir+") do bin\\cypher-shell -u neo4j -p 12345678 -d "+database+" --file %f");
        commands.add("for %f in ("+edge_dir+") do bin\\cypher-shell -u neo4j -p 12345678 -d "+database+" --file %f");
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
        return Result.ok();
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
            fileDtoList.add(new FileDto(file1.getName(),file1.getPath()));
        }
        return Result.ok(fileDtoList,files.length);
    }
}
