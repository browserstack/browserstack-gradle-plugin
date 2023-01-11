PWD=`pwd`.strip

def run_command(s)
  stdout = `#{s} 2>&1`
  return stdout
end

def print_separator
  puts "\n******************************************************************************************************"
end

def setup_required_paths
  $current_path = $current_path.chop
  $correct_absolute_mainAPKPath =  $current_path + "/test/mainApk"
  $incorrect_absolute_APKPath = "/random_path/"
  $correct_absolute_testAPKPath =  $current_path + "/test/testApk"
  $correct_relative_mainAPKPath = "./test/mainApk"
  $incorrect_relative_APKPath = "./random_path/"
  $correct_relative_testAPKPath = "./test/testApk"
end

def setup_repo
  puts "Setting up sample repo"
  run_command("git clone https://github.com/browserstack/espresso-browserstack.git")
  Dir.chdir "espresso-browserstack"
  $current_path = run_command("pwd");
  run_command("git checkout gradlePluginTestBranch")
end

def setup_repo_with_app_variants
  puts "Adding gradle file with flavors."
  run_command("rm app/build.gradle")
  run_command("mv app/build-with-flavours.gradle app/build.gradle")
end

def run_basic_espresso_test(gradle_command)
  puts "Running #{gradle_command} with basic config"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/app_url|test_suite_url|build_id/)}
  if responses.count != 3
    puts "✘ #{gradle_command} failed with error: #{stdout}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_espresso_test_with_path(gradle_command)
  puts "Running #{gradle_command} with config and path to main and test apk"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/app_url|test_suite_url|build_id/)}
  if responses.count != 3
    puts "✘ #{gradle_command} failed with error: #{responses}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_espresso_test_with_invalid_cucumber_config(gradle_command)
  puts "Running #{gradle_command} with invalid cucumber config and path to main and test apk"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/app_url|test_suite_url|422|BROWSERSTACK_INVALID_VALUES_IN_INPUT/)}
  if responses.count != 4
    puts "✘ #{gradle_command} failed with error: #{responses}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_espresso_test_with_invalid_instrumentation_config(gradle_command)
  puts "Running #{gradle_command} with invalid instrumentation config and path to main and test apk"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/app_url|test_suite_url|422|contain characters/)}
  if responses.count != 4
    puts "✘ #{gradle_command} failed with error: #{responses}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_espresso_test_with_incorrect_path(gradle_command)
  puts "Running #{gradle_command} with basic config and incorrect paths to both main and test apk"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/DebugApp apk: null|TestApp apk: null/)}
  if responses.count != 2
    puts "✘ #{gradle_command} failed with error: #{responses}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_espresso_test_with_either_main_or_test_apk_path(gradle_command, apk_path)
  puts "Running #{gradle_command} with basic config and either main apk path or test apk path"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/#{apk_path}|test_suite_url|build_id/)}
  if responses.count != 3
    puts "✘ #{gradle_command} failed with error: #{responses}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_espresso_test_with_one_absolute_and_one_relative_apk_path(gradle_command, main_apk_path, test_apk_path)
  puts "Running #{gradle_command} with basic config and one of the paths as absolute and one as relative"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/#{main_apk_path}|#{test_apk_path}|test_suite_url|build_id/)}
  if responses.count != 4
    puts "✘ #{gradle_command} failed with error: #{responses}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_app_live_test(gradle_command)
  puts "Running #{gradle_command}"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/app_url|test_suite_url|build_id/)}
  if responses.empty?
    puts "✘ #{gradle_command} failed with error: #{stdout}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_tests
  puts "\nRunning new tests using ./gradlew"
  run_basic_espresso_test("./gradlew clean executeDebugTestsOnBrowserstack")
  print_separator
  puts "\n"
  run_app_live_test("./gradlew clean uploadDebugToBrowserstackAppLive")
  print_separator
end

def run_tests_args
  puts "\nRunning new tests using ./gradlew with args"
  run_basic_espresso_test("./gradlew clean executeDebugTestsOnBrowserstack --config-file='command-line-config-browserstack.json'")
  run_basic_espresso_test("./gradlew executeDebugTestsOnBrowserstack -PskipBuildingApks=true")
  run_basic_espresso_test("./gradlew executeDebugTestsOnBrowserstack -PskipBuildingApks=false")
  print_separator
end

def run_tests_with_path_args
  puts "\nRunning new test using ./gradlew with APK paths for main and test apk"
  mainAPKPath =  $current_path + "/test/mainApk"
  testAPKPAth =  $current_path + "/test/testApk"
  run_espresso_test_with_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth}")
end

def run_tests_with_relative_path
  puts "\nRunning new test using ./gradlew with relative paths for both main and test apk"
  mainAPKPath = "./test/mainApk"
  testAPKPAth = "./test/testApk"
  run_espresso_test_with_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth}")
end

def run_test_with_incorrect_path
  puts "\nRunning new test using ./gradlew with incorrect main and test APK paths"
  mainAPKPath =  $current_path
  testAPKPAth =  $current_path
  run_espresso_test_with_incorrect_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth}")
end

def run_test_with_incorrect_relative_path
  puts "\nRunning new test using ./gradlew with incorrect relative paths for both main and test APK "
  mainAPKPath = "./test"
  testAPKPAth = "./test"
  run_espresso_test_with_incorrect_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth}")
end


def run_tests_with_path_variations
 puts "\nRunning new test using ./gradlew with mainAPKPath arg only"
 mainAPKPath =  $current_path + "/test/mainApk"
 run_espresso_test_with_either_main_or_test_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath}", mainAPKPath);
 puts "\nRunning new test using ./gradlew with testAPKPath arg only"
 testAPKPAth =  $current_path + "/test/testApk"
 run_espresso_test_with_either_main_or_test_apk_path("./gradlew executeDebugTestsOnBrowserstack -PtestAPKPath=#{testAPKPAth}", testAPKPAth);
end

def run_tests_with_relative_path_variations
  puts "\nRunning new test using ./gradlew with mainAPKPath(relative path) arg only"
  mainAPKPath = "./test/mainApk"
  run_espresso_test_with_either_main_or_test_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath}", mainAPKPath);
  puts "\nRunning new test using ./gradlew with testAPKPath(relative path) arg only"
  testAPKPAth = "./test/testApk"
  run_espresso_test_with_either_main_or_test_apk_path("./gradlew executeDebugTestsOnBrowserstack -PtestAPKPath=#{testAPKPAth}", testAPKPAth);
  puts "\nRunning with absolute main and relative test path"
  mainAPKPath =  $current_path + "/test/mainApk"
  run_espresso_test_with_one_absolute_and_one_relative_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth}", mainAPKPath, testAPKPAth)
  puts "\nRunning with absolute test and relative main path"
  testAPKPAth =  $current_path + "/test/testApk"
  mainAPKPath = "./test/mainApk"
  run_espresso_test_with_one_absolute_and_one_relative_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth}", mainAPKPath, testAPKPAth)
end

def run_espresso_test_with_non_existing_apk_path(gradle_command, apk_type)
  puts "Running #{gradle_command} with non existing path"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/Invalid File Path: Please provide a valid #{apk_type} APK path/)}
  if responses.count != 1
    puts "✘ #{gradle_command} failed with error: #{responses}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_espresso_test_with_one_existing_and_one_non_existing_apk_path(gradle_command, incorrect_apk_type)
  puts "Running #{gradle_command}"
  stdout = run_command(gradle_command)
  response_line_with_incorrect_path = stdout.lines.select{ |line| line.match(/Invalid File Path: Please provide a valid #{incorrect_apk_type} APK path/)}
  response_line_with_build_id = stdout.lines.select{ |line| line.match(/build_id/)}
  if response_line_with_incorrect_path.count != 1 && response_line_with_build_id != 0
    puts "✘ #{gradle_command} failed with error: #{response_line_with_incorrect_path}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts response_line_with_incorrect_path.join("\n")
  end
end

def run_espresso_test_with_one_incorrect_apk_path(gradle_command)
  puts "Running #{gradle_command}"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/TestApp apk: null/)}
  if responses.count != 1
    puts "✘ #{gradle_command} failed with error: #{responses}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_test_with_non_existing_path
   puts "\nRunning new test with non existing absolute main path"
   mainAPKPath = "/random_path/"
   run_espresso_test_with_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath}", "main");
   puts "\nRunning new test with non existing relative main path"
   mainAPKPath = "./random_path/"
   run_espresso_test_with_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath}", "main");
   puts "\nRunning new test with non existing absolute test path"
   testAPKPath = "/random_path/"
   run_espresso_test_with_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PtestAPKPath=#{testAPKPath}", "test");
   puts "\nRunning new test with non existing absolute test path"
   testAPKPath = "./random_path/"
   run_espresso_test_with_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PtestAPKPath=#{testAPKPath}", "test");
end

def run_test_with_one_correct_and_one_non_existing_path
   puts "\nRunning new test with absolute correct test and non existing absolute main path"
   run_espresso_test_with_one_existing_and_one_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{$incorrect_absolute_APKPath} -PtestAPKPath=#{$correct_absolute_testAPKPath}", "main");
   puts "\nRunning new test with relative correct test and non existing absolute main path"
   run_espresso_test_with_one_existing_and_one_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{$incorrect_absolute_APKPath} -PtestAPKPath=#{$correct_relative_testAPKPath}", "main");
   puts "\nRunning new test with absolute correct test and non existing relative main path"
   run_espresso_test_with_one_existing_and_one_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{$incorrect_relative_APKPath} -PtestAPKPath=#{$correct_absolute_testAPKPath}", "main");
   puts "\nRunning new test with relative correct test and non existing relative main path"
   run_espresso_test_with_one_existing_and_one_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{$incorrect_relative_APKPath} -PtestAPKPath=#{$correct_relative_testAPKPath}", "main");
   puts "\nRunning new test with absolute correct main and non existing absolute test path"
   run_espresso_test_with_one_existing_and_one_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{$correct_absolute_mainAPKPath} -PtestAPKPath=#{$incorrect_absolute_APKPath}", "test");
   puts "\nRunning new test with absolute correct main and non existing relative test path"
   run_espresso_test_with_one_existing_and_one_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{$correct_absolute_mainAPKPath} -PtestAPKPath=#{$incorrect_relative_APKPath}", "test");
   puts "\nRunning new test with relative correct main and non existing absolute test path"
   run_espresso_test_with_one_existing_and_one_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{$correct_relative_mainAPKPath} -PtestAPKPath=#{$incorrect_absolute_APKPath}", "test");
   puts "\nRunning new test with relative correct main and non existing relative test path"
   run_espresso_test_with_one_existing_and_one_non_existing_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{$correct_relative_mainAPKPath} -PtestAPKPath=#{$incorrect_relative_APKPath}", "test");
end

def run_test_with_ipa
  puts "Running tests with ipa files"
  mainAPKPath =  $current_path + "/test/mainApk/ipa"
  testAPKPAth =  $current_path + "/test/mainApk/ipa"
  run_espresso_test_with_incorrect_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth}")
end

def run_test_with_zip
  puts "Running tests with ipa files"
  mainAPKPath =  $current_path + "/test/mainApk/"
  testAPKPAth =  $current_path + "/test/mainApk/zip"
  run_espresso_test_with_one_incorrect_apk_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth}");
end

def run_tests_with_flavors
  puts "Running tests with flavors using ./gradlew"
  run_basic_espresso_test("./gradlew clean executePhoneDebugTestsOnBrowserstack")
  print_separator
  puts "\n"
  run_app_live_test("./gradlew clean uploadPhoneDebugToBrowserstackAppLive")
  print_separator
end

def remove_repo
  Dir.chdir PWD
  run_command("rm -rf espresso-browserstack")
end

def build_plugin
  puts "Building gradle plugin using ./gradlew"
  run_command("./gradlew clean build")
end

def validate_env
  missing_env_variables = []
  requied_env_variables = ["ANDROID_HOME","BROWSERSTACK_USERNAME", "BROWSERSTACK_ACCESS_KEY"]
  requied_env_variables.each do |env_variable|
    if ENV[env_variable].nil?
       missing_env_variables += [env_variable]
    end
  end
  if !missing_env_variables.empty?
    raise "Please export #{missing_env_variables.join(',')}"
  end
end


def run_cli_tests
  puts "\nRunning CLI tests using ./gradlew with args"
  run_cli_test_help_command("./gradlew browserstackCLIWrapper -Pcommand='help'");
  run_cli_test_delete_command("./gradlew browserstackCLIWrapper -Pcommand='app-automate apps delete -a bs://3fc4eea395ea6efc69b74e8211ecf2eba8879373'")
  print_separator
end

def run_cli_test_help_command(gradle_command)
  puts "Running #{gradle_command}"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/BrowserStack-cli|authenticate|app-automate/)}
  if responses.count != 3
    puts "✘ #{gradle_command} failed with error: #{stdout}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_cli_test_delete_command(gradle_command)
  puts "Running #{gradle_command}"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/BROWSERSTACK_APP_NOT_FOUND/)}
  if responses.count != 1
    puts "✘ #{gradle_command} failed with error: #{stdout}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_test_with_cucumber_options
  puts "\nRunning new test using ./gradlew with absolute APK paths with cucumber options"
  mainAPKPath =  $current_path + "/test/mainApk/cucumber"
  testAPKPAth =  $current_path + "/test/testApk/cucumber"
  run_espresso_test_with_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth} --config-file=config-browserstack_cucumber.json")
  puts "\nRunning new test using ./gradlew with absolute APK paths with invalid cucumber options"
  run_espresso_test_with_invalid_cucumber_config("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth} --config-file=config-browserstack_cucumber_invalid_name.json")
  puts "\nRunning new test using ./gradlew with relative APK paths with cucumber options"
  mainAPKPath =  "./test/mainApk/cucumber"
  testAPKPAth =  "./test/testApk/cucumber"
  run_espresso_test_with_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth} --config-file=config-browserstack_cucumber.json")
  puts "\nRunning new test using ./gradlew with relative APK paths with invalid cucumber options"
  run_espresso_test_with_invalid_cucumber_config("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth} --config-file=config-browserstack_cucumber_invalid_name.json")
end

def run_test_with_instrumentation_options
  puts "\nRunning new test using ./gradlew with absolute APK paths with instrumentation options"
  mainAPKPath =  $current_path + "/test/mainApk/cucumber"
  testAPKPAth =  $current_path + "/test/testApk/cucumber"
  run_espresso_test_with_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth} --config-file=config-browserstack_instrumentation.json")
  puts "\nRunning new test using ./gradlew with absolute APK paths with invalid instrumentation options"
  run_espresso_test_with_invalid_instrumentation_config("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth} --config-file=config-browserstack_instrumentation_invalid.json")
  puts "\nRunning new test using ./gradlew with relative APK paths with instrumentation options"
  mainAPKPath =  "./test/mainApk/cucumber"
  testAPKPAth =  "./test/testApk/cucumber"
  run_espresso_test_with_path("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth} --config-file=config-browserstack_instrumentation.json")
  puts "\nRunning new test using ./gradlew with relative APK paths with invalid instrumentation options"
  run_espresso_test_with_invalid_instrumentation_config("./gradlew executeDebugTestsOnBrowserstack -PmainAPKPath=#{mainAPKPath} -PtestAPKPath=#{testAPKPAth} --config-file=config-browserstack_instrumentation_invalid.json")
end

def test
  validate_env
  build_plugin
  setup_repo
  setup_required_paths
  run_tests
  remove_repo
  setup_repo
  setup_required_paths
  run_tests_args
  run_tests_with_path_args
  run_test_with_incorrect_path
  run_tests_with_path_variations
  run_tests_with_relative_path
  run_test_with_incorrect_relative_path
  run_tests_with_relative_path_variations
  run_test_with_non_existing_path
  run_test_with_one_correct_and_one_non_existing_path
  run_test_with_ipa
  run_test_with_zip
  run_test_with_cucumber_options
  run_test_with_instrumentation_options
  run_cli_tests
  setup_repo_with_app_variants
  run_tests_with_flavors
  remove_repo
end

class String
  def red
    "\e[#{31}m#{self}\e[0m"
  end

  def green
    "\e[#{32}m#{self}\e[0m"
  end
end

test
