PWD=`pwd`.strip

def run_command(s)
  stdout = `#{s} 2>&1`
  return stdout
end

def print_separator
  puts "\n******************************************************************************************************"
end

def setup_repo
  puts "Setting up sample repo"
  run_command("git clone https://github.com/browserstack/espresso-browserstack.git")
  Dir.chdir "espresso-browserstack"
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
  print_separator
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

def test
  validate_env
  build_plugin
  setup_repo
  run_tests
  remove_repo
  setup_repo
  run_tests_args
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
