#!/usr/bin/env ruby

M2_REPO = "#{Dir.home}/.m2/repository"
BOOTCOMPILER = File.expand_path('../../mozart2/bootcompiler', __FILE__)

contents = File.read('.classpath')
entries = contents.scan(/<classpathentry kind="(?:var|lib|output)" path="([^"]+)"/)
classpath = entries.map { |path,|
  path.sub("M2_REPO", M2_REPO).sub("/bootcompiler", BOOTCOMPILER)
}.join(':')

cmd = nil

if ARGV.empty?
  cmd = ['java', '-cp', classpath, 'org.mozartoz.truffle.Main']
  puts "$ #{cmd.join(' ')}"
  exec(*cmd)
elsif ARGV == %w[classpath]
  puts classpath
  exit
elsif ARGV == %w[compile]
  cmd = %w[javac -sourcepath src -cp] + [classpath] + %w[-d bin] + Dir["src/**/*.java"]
  exec(*cmd)
end
