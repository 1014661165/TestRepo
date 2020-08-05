package com.fudan.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Git工具类
 */
public class GitManager {

    private static Map<String, Git> repoMap = new HashMap<>();
    private static Map<String, RevWalk> repoWalkMap = new HashMap<>();
    private static Map<String, TreeWalk> repoTreeMap = new HashMap<>();

    /**
     * 初始化git库
     *
     * @param repos
     */
    public static void init(List<String> repos){
        for (String repo : repos) {
            try {
                Git git = Git.open(new File(repo));
                repoMap.put(repo, git);
                repoWalkMap.put(repo, new RevWalk(git.getRepository()));
                repoTreeMap.put(repo, new TreeWalk(git.getRepository(), git.getRepository().newObjectReader()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放git库
     */
    public static void release() {
        if (repoMap == null) {
            return;
        }
        for (String repo : repoMap.keySet()) {
            repoMap.get(repo).close();
            repoWalkMap.get(repo).close();
            repoTreeMap.get(repo).close();
        }
    }

    /**
     * 获取项目最新的commit
     *
     * @return
     */
    public static Map<String, String> getLatestCommit() {
        Map<String, String> latestCommits = new HashMap<>();
        for (String repo : repoMap.keySet()) {
            String commit = "";
            try {
                Git git = repoMap.get(repo);
                Iterator<RevCommit> commits = git.log().call().iterator();
                commit = commits.next().name();
            } catch (Exception e) {
                e.printStackTrace();
            }
            latestCommits.put(repo, commit);
        }
        return latestCommits;
    }

    /**
     * 获取两次commit之间发生变化的文件列表
     *
     * @param repo          项目
     * @param earlyCommit   较早的commit
     * @param currentCommit 当前的commit
     * @return
     */
    public static Map<String, String> getUpdateFiles(String repo, String earlyCommit, String currentCommit) {
        Map<String, String> updateFiles = new HashMap<>();
        try {
            Git git = repoMap.get(repo);
            RevCommit early = searchCommit(repo, earlyCommit);
            RevCommit current = searchCommit(repo, currentCommit);
            AbstractTreeIterator earlyTree = getAbstractTreeIterator(early, git.getRepository());
            AbstractTreeIterator currentTree = getAbstractTreeIterator(current, git.getRepository());
            List<DiffEntry> diffEntries = git.diff().setOldTree(earlyTree).setNewTree(currentTree).call();
            for (DiffEntry diffEntry : diffEntries) {
                updateFiles.put(diffEntry.getNewPath(), diffEntry.getOldPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updateFiles;
    }

    /**
     * 根据commit获得RevCommit对象
     *
     * @param repo
     * @param commit
     * @return
     */
    private static RevCommit searchCommit(String repo, String commit) {
        Git git = repoMap.get(repo);
        RevCommit target = null;
        RevWalk revWalk = null;
        try {
            Repository repository = git.getRepository();
            revWalk = new RevWalk(repository);
            target = revWalk.parseCommit(ObjectId.fromString(commit));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (revWalk != null) {
                revWalk.close();
            }
        }
        return target;
    }


    /**
     * 获取某次commit的Tree
     *
     * @param commit
     * @param repository
     * @return
     */
    private static AbstractTreeIterator getAbstractTreeIterator(RevCommit commit, Repository repository) {
        RevWalk revWalk = new RevWalk(repository);
        CanonicalTreeParser treeParser = null;
        try {
            RevTree revTree = revWalk.parseTree(commit.getTree().getId());
            treeParser = new CanonicalTreeParser();
            treeParser.reset(repository.newObjectReader(), revTree.getId());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            revWalk.dispose();
        }
        return treeParser;
    }

    public static void getContent(String repo, String commit, String realPath, String outputFile){
        try {
            Git git = repoMap.get(repo);
            RevWalk revWalk = repoWalkMap.get(repo);
            TreeWalk treeWalk = repoTreeMap.get(repo);
            realPath = getVersedRelativePath(realPath);
            ObjectId lastCommitId = git.getRepository().resolve(commit);
            revWalk.reset();
            RevCommit revCommit = revWalk.parseCommit(lastCommitId);
            treeWalk.reset();
            treeWalk.addTree(revCommit.getTree());
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(realPath));
            if (!treeWalk.next()) {
                return;
            }
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = git.getRepository().open(objectId);
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            loader.copyTo(outputStream);
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String getVersedRelativePath(String relativePath){
        return relativePath.replaceAll("\\\\", "/");
    }
}
