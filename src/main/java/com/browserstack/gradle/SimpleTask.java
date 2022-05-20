package com.browserstack.gradle;

import org.gradle.api.tasks.TaskAction;
import com.browserstack.gradle.Constants;
import java.util.Map;
import java.nio.file.Path;

public class SimpleTask extends BrowserStackTask {

  @TaskAction
  void aSimpleTask() throws Exception {
    System.out.println("executing from within Simple Task");
  }
}
