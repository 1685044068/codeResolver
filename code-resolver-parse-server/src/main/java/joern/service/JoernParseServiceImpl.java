package joern.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import joern.utils.StreamGobbler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

@Service
public class JoernParseServiceImpl {
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
    public String parse(String url){
        List<String> commands=new ArrayList<>();
        int i=0;
        System.setProperty("joern",joern_dir);
        System.setProperty("import_db",toDb_dir);
        commands.add("rmdir " + joern_dir + "\\out" );//这里需要先把out删了再后面创建
        commands.add("joern-parse "+url+" -o "+cpg_dir);
        commands.add("joern-export.bat --repr=all --format=neo4jcsv "+cpg_dir);//这里好像joern里如果存在joern_dir则不会覆盖
        commands.add("move  "+source_dir+" "+import_dir);//删除joern\out后可正常运行
        System.out.println("source:" + source_dir + " ////  import_dir:" + import_dir);
        commands.add("for %f in ("+node_dir+") do bin\\cypher-shell -u neo4j -p chenqiujian123 -d mvc --file %f");
        commands.add("for %f in ("+edge_dir+") do bin\\cypher-shell -u neo4j -p chenqiujian123 -d mvc --file %f");
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
        return "success";
    }
}
