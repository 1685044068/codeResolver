package com.icbc.coderesolvergitoperationserver.service;

import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.icbc.codeResolver.entity.neo4jNode;
import com.icbc.codeResolver.service.CodeResolverService;
import com.icbc.codeResolver.service.GitOperationService;
import com.icbc.coderesolvergitoperationserver.util.GitUtil;
import jakarta.annotation.Resource;
import org.apache.commons.io.FileUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.forceDeleteOnExit;

@Service
@DubboService(group = "git",timeout =  1000000)
@RefreshScope
public class GitOperationServiceImpl implements GitOperationService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${git.username}")
    private String username;
    @Value("${git.password}")
    private String password;
    @Value("${git.local_path}")
    private String localPath;
    @DubboReference(group = "joern")
    CodeResolverService codeResolverService;


    @Override
    public List<neo4jNode> connectionAndPull(String remoteURI, String branch) throws GitAPIException,IOException{
        //.git文件路径

        System.out.println(username);
        //建立与远程仓库的联系，仅需要执行一次
        String msg = "";

        File root = new File(localPath);
//        if (root.exists()) {
//            deleteDirectory(root);
//            if (root.exists()){
//                localPath+="planB\\";
//            }
//        }
        System.out.println(localPath);
//        root= new File(localPath);
        String logPath = localPath + ".git";
        Git git=null;
        Repository repository=null;
        try {
            File temp=new File(logPath);
            if(!temp.exists()){
                Git.cloneRepository().setURI(remoteURI).setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).setBranch(branch).setDirectory(new File(localPath)).call();
            }

            git = new Git(new FileRepository(logPath));
           git.pull().setRebase(true).setRemoteBranchName(branch).setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
        } catch (Exception e) {
            msg =e.getMessage();
        }
        System.out.println(msg);

        try {
            git = Git.open(root);
            repository = git.getRepository();
        } catch (IOException e) {
            e.printStackTrace();
        }
        RevWalk walk = new RevWalk(repository);
        //获取最近提交的两次记录
        Iterable<RevCommit> commits = git.log().setMaxCount(1).call();
        List<neo4jNode> listNode=null;
        for (RevCommit commit : commits) {
            System.out.println(commit.getId());
            System.out.println(stringRedisTemplate.opsForValue().get("commitId"));
            if (commit.getId()!=null&&commit.getId().toString().equals(stringRedisTemplate.opsForValue().get("commitId"))){
                Gson gson = new Gson();
                Type listType = new TypeToken<List<neo4jNode>>(){}.getType();
                listNode = gson.fromJson(stringRedisTemplate.opsForValue().get("listNode"), listType);
                System.out.println("ok!!!");
            }else{
                listNode=getRow();
                stringRedisTemplate.opsForValue().set("listNode",JSONUtil.toJsonStr(listNode));
            }
        }
        walk.close();
        repository.close();
        git.close();
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return listNode;
    }


    public List<neo4jNode>  getRow() throws GitAPIException,IOException {
        String gitFilePath = localPath + ".git";

        File root = new File(gitFilePath);
        Git git=null;
        Repository repository=null;
        try {
            git = Git.open(root);
            repository = git.getRepository();

        } catch (IOException e) {
            e.printStackTrace();
        }
        RevWalk walk = new RevWalk(repository);
        List<RevCommit> commitList = new ArrayList<>();
        //获取最近提交的两次记录
        Iterable<RevCommit> commits = git.log().setMaxCount(2).call();
        for (RevCommit commit : commits) {
            commitList.add(commit);
        }
        stringRedisTemplate.opsForValue().set("commitId",commitList.get(0).getId().toString());
        Map<String,List<Integer>> map=new HashMap<>();
        if (commitList.size() == 2) {
            AbstractTreeIterator newTree = GitUtil.prepareTreeParser(commitList.get(0), repository);
            AbstractTreeIterator oldTree = GitUtil.prepareTreeParser(commitList.get(1), repository);
            List<DiffEntry> diff = git.diff().setOldTree(oldTree).setNewTree(newTree).setShowNameAndStatusOnly(true).call();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(out);
            //设置比较器为忽略空白字符对比（Ignores all whitespace）
            df.setContext(1);
            df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            df.setRepository(git.getRepository());
//            System.out.println("------------------------------start-----------------------------");
            //每一个diffEntry都是第个文件版本之间的变动差异
            for (DiffEntry diffEntry : diff) {

                //打印文件差异具体内容
                df.format(diffEntry);
                StringBuilder stringBuilder=new StringBuilder(diffEntry.getOldPath().replace("src/",""));
                System.out.println("***"+diffEntry.getOldPath().replace("src/",""));
                String diffText = out.toString("UTF-8");
                System.out.println(diffText);
                List<Integer> rowList=new ArrayList<>();
                String[] split = diffText.split("@@ -");
                for (int i = 1; i < split.length; i++) {
                    int index = split[i].indexOf(',');
                    rowList.add(Integer.parseInt(split[i].substring(0, index))+1);
                    System.out.println(Integer.parseInt(split[i].substring(0, index))+1);
                }
                map.put(stringBuilder.toString(),rowList);
                out.reset();
            }
            df.close();
            out.close();
        }
        walk.close();
        repository.close();
        git.close();
        System.gc();
        System.out.println(map);
        List<neo4jNode> dynamicData = codeResolverService.getDynamic(map);
        return dynamicData;
    }
}
