
## Building

`tui-scala` helps dog-food the experimental [bleep](https://bleep.build/docs/) Scala build tool while it is approaching first public release. Keep an open mind!

- `git clone https://github.com/oyvindberg/tui-scala`
- [install bleep](https://bleep.build/docs/installing/)
- (if you use bash, run `bleep install-tab-completions-bash` and start a new shell to get tab completions)
- `git submodule init && git submodule update`
- `bleep setup-ide jvm213` to enable IDE import (metals or intellij)
-  open in your IDE
- `bleep run demo@jvm213 <demoname>` to run demos
- `bleep gen-native-image` if you want to see how fast things get with native image compilation
