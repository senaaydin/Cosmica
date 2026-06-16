source "https://rubygems.org"

# Pin fastlane so local and CI runs resolve the same toolchain.
gem "fastlane", "~> 2.227"

# Load fastlane plugins declared in fastlane/Pluginfile (Firebase App Distribution).
plugins_path = File.join(File.dirname(__FILE__), "fastlane", "Pluginfile")
eval_gemfile(plugins_path) if File.exist?(plugins_path)
