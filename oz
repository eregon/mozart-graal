#!/usr/bin/env ruby

require 'pathname'

dir = File.expand_path('..', __FILE__)

BOOTCOMPILER = File.expand_path('../mozart2/bootcompiler', dir)
BOOTCOMPILER_JAR = "#{BOOTCOMPILER}/target/scala-2.11/bootcompiler-assembly-2.0-SNAPSHOT.jar"

TRUFFLE_API_JAR = File.expand_path("../truffle/mxbuild/dists/truffle-api.jar", dir)

BOOTCLASSPATH = [TRUFFLE_API_JAR]
maven_classpath = File.read(".classpath").scan(%r{kind="lib" path="([^"]+/\.m2/repository/[^"]+)"}).map(&:first)
CLASSPATH = [BOOTCOMPILER_JAR, "bin"] + maven_classpath

java_opts = %w[-ea -esa]
java = if ARGV.delete('--graal')
  graal_home = Dir["#{dir}/../jvmci/jdk1.8.0_*/product"].first
  unless graal_home
    system("rake build:graal")
    graal_home = Dir["#{dir}/../jvmci/jdk1.8.0_*/product"].first
  end
  File.expand_path("#{graal_home}/bin/java")
else
  'java'
end

args = ARGV.drop_while { |arg|
  if arg.start_with? '-'
    java_opts << arg
  end
}

cmd = [
  java,
  *java_opts,
  "-Xbootclasspath/p:" + BOOTCLASSPATH.join(':'),
  '-cp', CLASSPATH.join(':'),
  'org.mozartoz.truffle.Main'
] + args

puts "$ #{cmd.join(' ')}"
exec(*cmd)
