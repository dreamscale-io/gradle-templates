package org.dreamscale.templates

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.StoredConfig
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.*;

class GitRepo {

    private static final String GITHUB_BASE_URL = "https://github.com/dreamscale-io/"

    private Git git

    private GitRepo(Git git) {
        this.git = git
    }

    File getRepoDir() {
        git.repository.directory.parentFile
    }

    void commitProjectFiles(String commitMessage) {
        git.add().addFilepattern(".").call()
        if (git.status().call().clean == false) {
            git.commit().setMessage(commitMessage).call()
        }
    }

    void setRemoteUrl(String name, String url) {
        StoredConfig config = git.repository.config
        config.setString("remote", name, "url", url)
        config.save()
    }

    void pushProject(String username, String password) {
        Iterable<PushResult> results = git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).setPushAll().call()
        for (PushResult result : results) {
            parsePushResult(result)
        }
    }

    private static void parsePushResult(PushResult result) {
        for (RemoteRefUpdate remoteRefUpdate : result.remoteUpdates) {
            if(isRemoteRefUpdateStatusFailed(remoteRefUpdate.status)) {
               throw new GitPushFailedException(remoteRefUpdate.message)
            }
        }
    }

    private static boolean isRemoteRefUpdateStatusFailed(RemoteRefUpdate.Status status) {
        switch (status) {
            case OK:
            case UP_TO_DATE:
            case NON_EXISTING:
                return false;
            case NOT_ATTEMPTED:
            case AWAITING_REPORT:
            case REJECTED_NODELETE:
            case REJECTED_NONFASTFORWARD:
            case REJECTED_REMOTE_CHANGED:
            case REJECTED_OTHER_REASON:
                return true;
            default:
                return true;
        }
    }

    static GitRepo init(File repoDir) {
        Git git = Git.init().setDirectory(repoDir).call()
        new GitRepo(git)
    }

    static GitRepo open(File repoDir) {
        Git git = Git.open(repoDir)
        new GitRepo(git)
    }

    static GitRepo openOrInitGitRepo(File repoDir, boolean clean) {
        if (clean) {
            repoDir.deleteDir()
        }
        repoDir.exists() ? openGitRepo(repoDir) : initGitHubRepo(repoDir.name, repoDir)
    }

    static GitRepo openGitRepo(File repoDir) {
        if (repoDir.exists() == false) {
            throw new RuntimeException("Cannot open git project, dir=${repoDir.absolutePath}")
        }
        open(repoDir)
    }

    static GitRepo initGitHubRepo(String name, File repoDir) {
        repoDir.mkdirs()
        GitRepo git = init(repoDir)
        git.setRemoteUrl("origin", GITHUB_BASE_URL + "${name}.git")
        git
    }

    static class GitPushFailedException extends RuntimeException {
        GitPushFailedException(String message) {
            super(message)
        }
    }

}
