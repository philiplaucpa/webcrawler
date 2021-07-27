package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws Exception {
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);
    System.out.println("config.getStartPages(): " + config.getStartPages());
    CrawlResult result = crawler.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);

    if (!config.getResultPath().isEmpty()) {
      System.out.println("Running Here");
      resultWriter.write(Path.of(config.getResultPath()));
    } else {
      System.out.println("Running Here Instead");
      Writer w=new BufferedWriter(new OutputStreamWriter(System.out));
      resultWriter.write(w);
    }

    if (!config.getProfileOutputPath().isEmpty()) {
      profiler.writeData(Path.of(config.getProfileOutputPath()));
    } else {
      profiler.writeData(new OutputStreamWriter(System.out));
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }

    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    System.out.println("ARGS[0]: " + args[0]);
    new WebCrawlerMain(config).run();
  }
}