LMIS_MOZ_DIR="#{Dir.pwd}/lmis-moz"

p LMIS_MOZ_DIR

def update_mis_moz
  if !Dir.exists?(LMIS_MOZ_DIR)
    setup_git_repo
  else
    system("cd #{LMIS_MOZ_DIR} && git checkout . && git pull -f origin master")
  end
end

def setup_git_repo
  system("git clone https://github.com/clintonhealthaccess/lmis-moz.git #{LMIS_MOZ_DIR}")
end

def start_jetty
  system("cd #{LMIS_MOZ_DIR} && ./build/startLocal.rb")
end

def stop_jetty
  puts "kill jetty"
  system("cd #{LMIS_MOZ_DIR} && ./build/stopLocal.sh")
end

def run_ft
  start_jetty
  puts "Start running contract test..."
  ctResult=system("./gradlew cT -P local")
  puts "Contract tests finished, start running functional test..."
  ftResult=system("./gradlew functionalTest")
  puts "functional tests finished"
  stop_jetty
  ctResult && ftResult
end