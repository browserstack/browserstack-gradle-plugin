PLUGIN_JAR_PATH=`pwd`.strip+"/build/libs/browserstack-gradle-plugin-2.0.0.jar"
PWD=`pwd`.strip

def run_command(s)
  stdout = `#{s} 2>&1`
  return stdout
end

def setup_repo
  run_command("git clone https://github.com/mohitmun/android-testing.git") 
  Dir.chdir "android-testing/ui/espresso/BasicSample"
  run_command("sed -i -e 's/PLUGIN_JAR_PATH/#{PLUGIN_JAR_PATH.gsub('/','\/')}/g' build.gradle")
end

def run_basic_espresso_test
  gradle_command = "gradle clean runDebugBuildOnBrowserstack"
  puts "Running #{gradle_command} with basic config"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/app_url|test_url|build_id/)}
  if responses.empty? && responses.count != 3
    puts "✘ #{gradle_command} failed with error: #{stdout}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_app_live_test
  gradle_command = "gradle clean uploadBuildToBrowserstackAppLive"
  puts "Running #{gradle_command} with"
  stdout = run_command(gradle_command)
  responses = stdout.lines.select{ |line| line.match(/app_url|test_url|build_id/)}
  if responses.empty?
    puts "✘ #{gradle_command} failed with error: #{stdout}".red
  else
    puts "✔ #{gradle_command} tests passed".green
    puts responses.join("\n")
  end
end

def run_tests
  run_basic_espresso_test
  puts "\n"
  run_app_live_test
end

def remove_repo
  Dir.chdir PWD
  run_command("rm -rf android-testing")
end

def build_plugin
  run_command("gradle clean build")
end

def test
  build_plugin
  setup_repo
  run_tests
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
