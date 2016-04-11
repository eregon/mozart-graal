#!/usr/bin/env ruby

M2_REPO = "#{Dir.home}/.m2/repository"
BOOTCOMPILER = File.expand_path('../../mozart2/bootcompiler', __FILE__)
BOOTCOMPILER_JAR = "#{BOOTCOMPILER}/target/scala-2.11/bootcompiler-assembly-2.0-SNAPSHOT.jar"

JAVA_HOME = File.expand_path(Dir["../jvmci/jdk1.8.0_*/product"].first)
JAVACMD = "#{JAVA_HOME}/bin/java"

unless File.exist?('.classpath')
  parent_dir = File.expand_path('../..', __FILE__)
  File.write('.classpath', File.read('tool/classpath').gsub('../', "#{parent_dir}/"))
  File.write('.factorypath', File.read('tool/factorypath').gsub('../', "#{parent_dir}/"))
end
contents = File.read('.classpath')
entries = contents.scan(/<classpathentry kind="(?:var|lib|output)" path="([^"]+)"/).map(&:first)
entries.unshift BOOTCOMPILER_JAR

bootclasspath, libraries = entries.map { |path|
  path.sub("M2_REPO", M2_REPO)
}.partition { |e| e.end_with?("truffle-api.jar") }

classpath = "#{bootclasspath.join(':')}:#{libraries.join(':')}"

if ARGV == %w[classpath]
  puts classpath
  exit
elsif ARGV == %w[compile]
  truffle_dsl_processor = bootclasspath[0].sub('/truffle-api.jar', '/truffle-dsl-processor.jar')
  cmd = %w[javac -sourcepath src -cp] + ["#{truffle_dsl_processor}:#{classpath}"] + %w[-d bin] + Dir["src/**/*.java"]
  exec(*cmd)
else
  java_opts = %w[-ea -esa]
  cmd = [JAVACMD, *java_opts, "-Xbootclasspath/p:#{bootclasspath.join(':')}", '-cp', libraries.join(':'), 'org.mozartoz.truffle.Main'] + ARGV
  puts "$ #{cmd.join(' ')}"
  exec(*cmd)
end
