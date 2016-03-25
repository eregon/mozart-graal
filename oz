#!/usr/bin/env ruby

M2_REPO = "#{Dir.home}/.m2/repository"
BOOTCOMPILER = File.expand_path('../../mozart2/bootcompiler', __FILE__)
BOOTCOMPILER_JAR = "#{BOOTCOMPILER}/target/scala-2.11/bootcompiler-assembly-2.0-SNAPSHOT.jar"

JAVA_HOME = File.expand_path(Dir["../jvmci/jdk1.8.0_*/product"].first)
JAVACMD = "#{JAVA_HOME}/bin/java"

unless File.exist?('.classpath')
  parent_dir = File.expand_path('../..', __FILE__)
  File.write('.classpath', File.read('tool/classpath').gsub('../', "#{parent_dir}/"))
end
contents = File.read('.classpath')
entries = contents.scan(/<classpathentry kind="(?:var|lib|output)" path="([^"]+)"/).map(&:first)
entries.unshift BOOTCOMPILER_JAR

bootclasspath, libraries = entries.map { |path|
  path.sub("M2_REPO", M2_REPO)
}.partition { |e| e.end_with?("truffle-api.jar") }

classpath = "#{bootclasspath.join(':')}:#{libraries.join(':')}"

cmd = nil

if ARGV == %w[classpath]
  puts classpath
  exit
elsif ARGV == %w[compile]
  cmd = %w[javac -sourcepath src -cp] + [classpath] + %w[-d bin] + Dir["src/**/*.java"]
  exec(*cmd)
else
  cmd = [JAVACMD, "-Xbootclasspath/p:#{bootclasspath.join(':')}", '-cp', libraries.join(':'), 'org.mozartoz.truffle.Main'] + ARGV
  puts "$ #{cmd.join(' ')}"
  exec(*cmd)
end
