# Bukkit-Command-System
**A small command System, organized in the fashion of a tree.**

_Description from the [Bukkit Thread](https://bukkit.org/threads/command-system.423765/ "Command System Thread")_

Hey,
I recently thought a command system would be a nice thing to develop, as I always find myself creating a bad one for every new plugin.
After a bit of arguing with myself, I thought I would create it in the fashion of a tree, as that allows child commands quite easily.

And now I am standing here, having developed a system, which can certainly be improved by a **lot**. (Noticed the emphasis?)

~~As I have absolutely no idea how to deal with git, you will find the source~~ *Still have no idea, but hey :P*

The basic System is as follows:
You have an abstract class called "CommandNode", which is a node in a Tree. This node offers a few methods, most of whom are private or protected, so you won't really need to deal with them :p
It has a few basic attributes (name, keyword, Pattern, permission) a parent, a list with children and a Predicate. The Predicate checks if the commandSender is accepted. I will say more about that later.

For the most things I said are public getters or validators (matchesPattern, hasPermission, acceptsCommandSender) available.

Then there are two more main methods.
One is "onTabComplete" which traverses all children recursively and tries to pick out the most fitting one. It also respects the permission and the acceptsCommandSender method.
The other is "execute". It does exactly this. It finds the most fitting node and lets it handle the execute method.


I also made 3 CommandNode classes, which are a bit more specialized. They are PlayerCommandNode, ConsoleCommandNode and BlockCommandNode. They will only allow their specific sender to execute or tab complete them and provide new abstract methods, supplying the correct sender.
So, in the PlayerCommandNode class you won't have an "execute(CommandSender", but an "execute(Player". This makes casting redundant.

One of the nicer things is that you can have multiple commands with the same keyword and/or pattern. This means, you can have two (or more) classes for the command "test" with the pattern "test". One of them extends ConsoleCommandNode, the other PlayerCommandNode.
Now, if a player executes "/basecommand test", it will execute the one extending PlayerCommandNode. If the console does it, it will execute the one extending ConsoleCommandNode.
This means, you can have two versions of one command, each in their own class, but one for players and one (possibly also taking a location) for the console.

The other main thing are subcommands. You can have "test" and two children "crash" and "dummy". Each of them is quite complex, which makes a if clause on the first argument quite messy.
With this system, you register "crash" and "dummy" as children of "test", and they will be available for tabcompletion and can also be executed just like test. They will be passed the correct arguments too. So "/basecommand test dummy hey" will execute "dummy" with the arguments "hey" and "/basecommand test crash" will just execute "crash".

Another thing is the help command. How would you do that? Well, the default implementation of the CommandListener thinks of that. You can add the "@HelpCommandNode" annotation to a CommandNode class, and it will be used as the main help command, if no valid command was detected. This happens for example, when the user just writes the baseCommand, without any additional parameters.

There are a few other useful classes. One is TreeCommandManager, which provides some useful checks for nodes, and is able to access package bound methods. It can handle most of the tasks you need.

The other ones are TreeCommandCommandListener and TreeCommandTabCompleteListener. They provide default implementations for handeling tab completes and commands, so you need to do nothing to make it work.
They each take a TreeCommandManager as constructor parameter, whose commands they will manage.
The CommandListener also takes a MessageProvider which is an interface in another (I18N) project. It just delivers the "no help node specified" and "no permission message". It may be better to put this in the same package and simplify it.

But enough to how it works. Here is how you basically add it:
Create the commands you want, extending CommandNode or a subclass of it.
Then you need to do some things in your onEnable or where you register the command:

```java
// create the tree manager
CommandTreeManager treeManager = new CommandTreeManager();
 
// register your commands
treeManager.registerChild(treeManager.getRoot(), new CommandExampleTestBlock());
 
// Currently: Create the mesage provider with the keys "no help node" and "no permission", as the constructor of CommandTreeCommandListener says.
// I used an I18N instance in my main plugin, as I use it everywhere else, too.
// Here is the important part of a dummy:
MessageProvider messageProvider = new MessageProvider() {
       
    @Override
    public String tr(String key, Object... formattingObjects) {
        if(key.equals("no help node")) {
            return ChatColor.RED + "No help node specified.";
        }
        else if(key.equals("no permission")) {
            return ChatColor.RED + "No permission!";
        }
        else {
            return "Unknown key: '" + key + "'";
        }
    }
// and so on. Just make some dummy methods or pass a real instance of a message provider. For a short test, a dummy is okay ;)
 
// register the executor and Listener
getCommand("exampleTest").setExecutor(new CommandTreeCommandListener(treeManager, messageProvider));
getCommand("exampleTest").setTabCompleter(new CommandTreeTabCompleteListener(treeManager, true));
``` 

And that was it. It *should* now work for you as good or bad as for me :p


I would absolutely love to hear some criticism, as long as it adds to the conversation! I think the term is constructive ;)

Nice you managed to read until here, have a nice day ;)
