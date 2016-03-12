#!/usr/bin/env ruby

M2_REPO = "#{Dir.home}/.m2/repository"
BOOTCOMPILER = File.expand_path('../../mozart2/bootcompiler', __FILE__)

contents = File.read('.classpath')
entries = contents.scan(/<classpathentry kind="(?:var|lib|output)" path="([^"]+)"/)
classpath = entries.map { |path,|
  path.sub("M2_REPO", M2_REPO).sub("/bootcompiler", BOOTCOMPILER)
}.join(':')

exec 'java', '-cp', classpath, 'org.mozartoz.truffle.Main'
