package io.bdrc.libraries;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class GitHelpers {

    public static final String gitignore = "# Ignore everything\n" + "*\n" + "# Don't ignore directories, so we can recurse into them\n" + "!*/\n"
            + "# Don't ignore .gitignore and *.foo files\n" + "!.gitignore\n" + "!*.trig\n" + "";
    public static final Map<String, String> typeToRepo = new HashMap<>();

    public static void createDirIfNotExists(String dir) {
        File theDir = new File(dir);
        if (!theDir.exists()) {
            // System.out.println("creating directory: " + dir);
            try {
                theDir.mkdir();
            } catch (SecurityException se) {
                System.err.println("could not create directory, please fasten your seat belt");
            }
        }
    }

    public static Map<String, Repository> typeRepo = new HashMap<>();

    public static void ensureGitRepo(String type, String REPOS_BASE_DIR) {
        if (typeRepo.containsKey(type))
            return;
        String dirpath = REPOS_BASE_DIR + type + "s-20220922";
        createDirIfNotExists(dirpath);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        File gitDir = new File(dirpath + "/.git");
        File wtDir = new File(dirpath);
        try {
            Repository repository = builder.setGitDir(gitDir).setWorkTree(wtDir)
                    // .setMustExist( true )
                    .readEnvironment() // scan environment GIT_* variables
                    .build();
            if (!repository.getObjectDatabase().exists()) {
                System.out.println("create git repository in " + dirpath);
                repository.create();
                //PrintWriter out = new PrintWriter(dirpath + "/.gitignore");
                //out.println(gitignore);
                //out.close();
            }
            typeRepo.put(type, repository);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Repository ensureGitRepo(String REPOS_BASE_DIR) {

        createDirIfNotExists(REPOS_BASE_DIR);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        File gitDir = new File(REPOS_BASE_DIR + "/.git");
        File wtDir = new File(REPOS_BASE_DIR);
        Repository repository = null;
        try {
            repository = builder.setGitDir(gitDir).setWorkTree(wtDir).readEnvironment().build();
            if (!repository.getObjectDatabase().exists()) {
                System.out.println("create git repository in " + REPOS_BASE_DIR);
                repository.create();
                //PrintWriter out = new PrintWriter(REPOS_BASE_DIR + "/.gitignore");
                //out.println(gitignore);
                //out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return repository;
    }

    public static Set<String> getChanges(String type) {
        Repository r = typeRepo.get(type);
        if (r == null) {
            System.out.println("getChanges DID NOT FIND REPO FOR " + type);
            return null;
        }
        Git git = new Git(r);
        Status status;
        Set<String> res = new HashSet<>();
        try {
            status = git.status().call();
        } catch (NoWorkTreeException | GitAPIException e) {
            e.printStackTrace();
            git.close();
            return null;
        }
        res.addAll(status.getModified());
        res.addAll(status.getAdded());
        git.close();
        return res;
    }

    public static RevCommit commitChanges(String type, String commitMessage) {
        Repository r = typeRepo.get(type);
        RevCommit rev = null;
        if (r == null) {
            return null;
        }
        Git git = new Git(r);
        try {
            if (!git.status().call().isClean()) {
                git.add().addFilepattern(".").call();
                rev = git.commit().setMessage(commitMessage).call();
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        git.close();
        return rev;
    }

    public static RevCommit commitChanges(Repository r, String commitMessage) {
        RevCommit rev = null;
        if (r == null) {
            return null;
        }
        Git git = new Git(r);
        try {
            if (!git.status().call().isClean()) {
                git.add().addFilepattern(".").call();
                rev = git.commit().setMessage(commitMessage).call();
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        git.close();
        return rev;
    }

    public static RevCommit commitDelete(String type, String deletePath, String commitMessage) {
        Repository r = typeRepo.get(type);
        RevCommit rev = null;
        if (r == null)
            return null;
        Git git = new Git(r);
        try {
            git.rm().addFilepattern(deletePath).call();
            if (!git.status().call().isClean()) {
                rev = git.commit().setMessage(commitMessage).call();
            }
            ;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        git.close();
        return rev;
    }

    public static Repository getLocalRepo(String type) {
        return typeRepo.get(type);
    }

    public static String getGitHeadFileContent(String repo, String filepath) throws IOException {
        Repository repository = new FileRepository(repo + "/.git");
        return getGitHeadFileContent(repository, filepath, null);
    }

    public static String getGitHeadFileContent(String repo, String filepath, String commit) throws IOException {
        Repository repository = new FileRepository(repo + "/.git");
        return getGitHeadFileContent(repository, filepath, commit);
    }

    public static String getGitHeadFileContent(Repository repository, String filepath, String commitString) throws IOException {
        ObjectId lastCommitId = null;
        if (commitString == null) {
            lastCommitId = repository.resolve(Constants.HEAD);
        } else {
            lastCommitId = repository.resolve(commitString);
        }
        // a RevWalk allows to walk over commits based on some filtering that is defined
        RevWalk revWalk = new RevWalk(repository);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        // and using commit's tree find the path
        RevTree tree = commit.getTree();
        // now try to find a specific file
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filepath));
        if (!treeWalk.next()) {
            revWalk.close();
            treeWalk.close();
            throw new IllegalStateException("Unable to download file.");
        }
        ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
        revWalk.close();
        treeWalk.close();
        return new String(loader.getBytes());
    }

    public static PullResult pull(String type, String REPOS_BASE_DIR)
            throws WrongRepositoryStateException, InvalidConfigurationException, InvalidRemoteException, CanceledException, RefNotFoundException,
            RefNotAdvertisedException, NoHeadException, TransportException, GitAPIException {
        GitHelpers.ensureGitRepo(type, REPOS_BASE_DIR);
        Git git = new Git(getLocalRepo(type));
        PullResult res = git.pull().setProgressMonitor(new TextProgressMonitor()).setRemote("origin").call();
        git.close();
        return res;
    }

    public static PullResult pull(Repository r) throws WrongRepositoryStateException, InvalidConfigurationException, InvalidRemoteException,
            CanceledException, RefNotFoundException, RefNotAdvertisedException, NoHeadException, TransportException, GitAPIException {
        Git git = new Git(r);
        PullResult res = git.pull().setProgressMonitor(new TextProgressMonitor()).setRemote("origin").call();
        git.close();
        return res;
    }

    public static String pull(String type) throws GitAPIException {
        Repository r = typeRepo.get(type);
        Git git = new Git(r);
        PullResult res = git.pull().setProgressMonitor(new TextProgressMonitor()).setRemote("origin").call();
        git.close();
        return Boolean.toString(res.isSuccessful());
    }

    public static Iterable<PushResult> push(String type, String REMOTE_BASE_URL, UsernamePasswordCredentialsProvider prov, String REPOS_BASE_DIR)
            throws InvalidRemoteException, TransportException, GitAPIException {
        GitHelpers.ensureGitRepo(type, REPOS_BASE_DIR);
        Git git = new Git(getLocalRepo(type));
        Iterable<PushResult> res = git.push().setCredentialsProvider(prov).setRemote(REMOTE_BASE_URL + type + "s").call();
        git.close();
        return res;
    }

    public static Iterable<PushResult> push(String type, String REMOTE_BASE_URL, String user, String pass, String REPOS_BASE_DIR)
            throws InvalidRemoteException, TransportException, GitAPIException {
        GitHelpers.ensureGitRepo(type, REPOS_BASE_DIR);
        Git git = new Git(getLocalRepo(type));
        Iterable<PushResult> res = git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pass))
                .setRemote(REMOTE_BASE_URL + type + "s").call();
        git.close();
        return res;
    }

    public static Iterable<PushResult> push(Repository r, String REMOTE_BASE_URL, String user, String pass)
            throws InvalidRemoteException, TransportException, GitAPIException {
        Git git = new Git(r);
        Iterable<PushResult> res = git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pass)).setRemote(REMOTE_BASE_URL)
                .call();
        git.close();
        return res;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(getGitHeadFileContent("/etc/buda/editserv/users/", "99/U1417245714.trig"));
    }

}
