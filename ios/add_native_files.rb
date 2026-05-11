#!/usr/bin/env ruby
# Run this from the ios/ directory: ruby add_native_files.rb
# It adds the KeyEventListener and EventWindow files to the Xcode project

require 'xcodeproj'

project_path = 'BtRemoteLogger.xcodeproj'
project = Xcodeproj::Project.open(project_path)

target = project.targets.find { |t| t.name == 'BtRemoteLogger' }
group = project.main_group.find_subpath('BtRemoteLogger', false)

files_to_add = [
  'BtRemoteLogger/EventWindow.h',
  'BtRemoteLogger/EventWindow.m',
  'BtRemoteLogger/KeyEventListener.h',
  'BtRemoteLogger/KeyEventListener.m',
]

files_to_add.each do |file_path|
  file_name = File.basename(file_path)

  existing = group.files.find { |f| f.display_name == file_name }
  if existing
    puts "#{file_name} already in project, skipping"
    next
  end

  ref = group.new_reference(file_path)
  ref.source_tree = '<group>'

  if file_path.end_with?('.m')
    target.source_build_phase.add_file_reference(ref)
    puts "Added #{file_name} to Sources"
  else
    puts "Added #{file_name} to project"
  end
end

project.save
puts "Done! Project saved."
