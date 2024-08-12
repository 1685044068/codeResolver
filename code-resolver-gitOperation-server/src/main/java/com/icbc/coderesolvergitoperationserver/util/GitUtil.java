package com.icbc.coderesolvergitoperationserver.util;

import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;


public class GitUtil {
    public static AbstractTreeIterator prepareTreeParser(RevCommit commit, Repository repository){
//        System.out.println(commit.getId());
        try (RevWalk walk = new RevWalk(repository)) {
//            System.out.println(commit.getTree().getId());
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            try (ObjectReader oldReader = repository.newObjectReader()) {
                oldTreeParser.reset(oldReader, tree.getId());
            }

            walk.dispose();

            return oldTreeParser;
        }catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }
}
