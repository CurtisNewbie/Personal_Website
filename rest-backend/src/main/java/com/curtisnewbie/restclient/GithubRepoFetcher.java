package com.curtisnewbie.restclient;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.curtisnewbie.persistence.RepoRepository;
import com.curtisnewbie.persistence.Repository;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;

/**
 * ------------------------------------
 * 
 * Author: Yongjie Zhuang
 * 
 * ------------------------------------
 * <p>
 * This class is responsible for fetching data of repositories from Github.
 * Which repositories to fetch are dependent on {@code config.repo.names} in
 * .properties.
 * </p>
 * 
 * @see GithubRepoFetcher#scheduledFetch()
 */
@ApplicationScoped
public class GithubRepoFetcher {
    private final Logger logger = Logger.getLogger(this.getClass());

    @ConfigProperty(name = "config.repo.names")
    protected List<String> repoNames;

    @Inject
    @RestClient
    protected GithubClient client;

    @Inject
    protected RepoRepository rrepo;

    /**
     * Start a new thread on app startup to fetch and update repositories
     * repeatively in every N minutes.
     * 
     * @param ev
     * @see {@link GithubRepoFetcher#freqInMin}
     */
    void onStart(@Observes StartupEvent ev) {
        logger.info(String.format("Initialising %s", this.getClass().getName()));
        logConfig();
    }

    /**
     * Fetch repo data in every 10 minutes
     */
    @Scheduled(every = "10m")
    protected void scheduledFetch() {
        if (repoNames.size() == 1 && repoNames.get(0).trim().equals("*")) {
            fetchAll();
        } else {
            for (String repo : repoNames)
                fetch(repo);
        }
    }

    /**
     * Fetch one repository
     * 
     * @param repoName
     */
    void fetch(String repoName) {
        // TODO: finish implentation
        client.fetchRepo("curtisnewbie", repoName).thenAccept((repoDto) -> {
            rrepo.updateRepo(new Repository(repoDto));
        });
    }

    /**
     * Fetch all repositories that are accessible
     */
    void fetchAll() {
        // TODO: finish implentation
        client.fetchAllRepos("curtisnewbie").thenAccept((list) -> {
            for (var repoDto : list) {
                rrepo.updateRepo(new Repository(repoDto));
            }
        });
    }

    /**
     * Log config used
     */
    private void logConfig() {
        logger.info(String.format("%s (config.repo.names) will fetch repositories: %s", this.getClass().getName(),
                this.repoNames.toString()));
    }
}