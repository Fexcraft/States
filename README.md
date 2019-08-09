# States
_A Successor to the FSU "Nations" series._    
(Wiki: https://github.com/Fexcraft/States/wiki)

### What is "States"?
Similarly to "Nations" (an old FSU module), it contains a _multi-layer_ protection system.    
It is composed as follows: `Chunk -> District -> Municipality -> State`.    

Players can create a "Municipality", technically regard it as a "town" which expands.    
Depending on it's size it will get various Titles ("Hamlet", "Village", "Town" and so on).    
You can configure those titles and aswel limit how many districts a municipality can have, in config.    

Here an example of how a populated/claimed area could look like:
![STCM](https://i.imgur.com/6KgjMei.png)


Chunks have "types", based on their type their protection is handled differently, chunks can also be linked    
to each other - the "core" chunk will store essential data then (owner, type, tax), and if you sell the core    
chunk, all linked to it are sold ofcourse too!

Here an example of various _Chunk Types_ in various districts:
![STPM](https://i.imgur.com/pDfBGTV.png)


By default "wilderness" (unclaimed land) is _protected_, making players unable to edit blocks there, you can    
ofcourse disable that in config. In case when the wilderness is protected players can make "temporary" claims    
which are called "Transit Zones" - they automatically unclaim after a day and cost 10% of the chunk's price.    
They are for example useful to e.g. build roads from one municipality to another which are a distance apart.    
_Ofcourse, the "transit zone" does not give access to the whole chunk, only a specific level set in config._


There are also other nice side-features like "Sign Ships", which can be either linked to the player himself    
(private player shops) or to the municipality or state - so the income/outcome goes directly there.    
_[SignShop - VIDEO](https://cdn.discordapp.com/attachments/424351061873131521/443900271362572288/2018-05-10_00-17-07.mp4)


### Other mentionable things:
- an special ingame GUI to claim land
- a GUI to see the whole region at once (32x32 chunks area) (note: currently instable)
  - available modes being "terrain", "claims", "districts", "municipalities", "states"
- every district/municipality/state can have a custom (HEX) color on the map! (or actually should)
- districts have also "types", based on them e.g. (once _Companies_ are implemented) buying terrain as Company     
will be limited based on your Company's type! (e.g. no industrial company in residental areas etc)
  - example types are - "commercial", "industrial", "residental", "agricultural" and a few more
- notification (GUI) upon entering a different area, e.g. on switching district, municipality or state!
- Discord Chat integration - see your ingame messages in discord - and send some back!
  - currently does need my bot [>link<](https://discordapp.com/api/oauth2/authorize?client_id=435505271108927533&permissions=8&scope=bot), once you got it use `|states` command to setup a channel
- **States** uses the FSMM Currency/Account Management
  - FSMM also allows you to set items from other mods/vanilla as valid money
    - FSMM also allows you to register those items as money the ATM can return
  - you can disable all the default FSMM items also if you do not like them and want your own
- Tax System and _other things I don't remember right now!_


If you're willing to help out - be it in code, assets or pure theorizing on new features/improvements - feel free    
to join our Discord Server! (although, it cannot be guaranteed new ideas will be welcomed instantly,    
depending on mood on that day)

**THIS MOD DOES NOT CONTAIN A "WAR" MODE**    
As some years of server management confirm, for one it's hard to have a "realistic" war in MC    
and for other if allowing it to be "minecrafty" - it's a strange mere clicking of swords,    
which is strange on itself and I never understood it.    
Not even Towny (bukkit plugin) had a war mode due to similar reasons - "how to do it??".


### Why "States" as name? Wasn't the "nations" one good enough?
It maybe was good, but a quick googling shows you this:
```
nation - english
a large aggregate of people united by common descent, history, culture, or language,
inhabiting a particular country or territory.
```
```
state - english
a nation or territory considered as an organized political community under one government.
e.g. "Germany, Italy, and other European states"
```
From past experience, people tended to either wanting to recreate a past time country - or went full nationalist    
on their made up ingame country, and that's not something I like to see and it constantly degraded gameplay.    
People instead of working together (or perhaps compete against) kept a constant a tense relation,    
and seriously, we're here to enjoy our free time, not recreate world problems in a blocky game?    
"States" is a more neutral name, a state is an organizational unit, you can assign towns/villages to it    
and the state's job is to keep all running smoothly, and eventually protect from current/future issues.

**Does that mean I cannot do something against that annoying neighbor?**    
Economy, eventually _economy_. "Comapnies" are a planned future feature.    
You'll be able to setup tolls or even banish companies from another State.    
Additionally if you would add some nice survival mods into a Server/Modpack with a large Map and keep    
wilderness-access off (like for example I (plan to) do), it can become a nice challenging gameplay,    
as people will need to trade some resources to get further, or play along nicely.    
_Well, at least in theory._


### Anything else I need to know?
_**Ask**, as I have partial memory loss, and already did forgot some of things I wanted to write. At least I think._

### Depencencies
- [FCL](https://github.com/Fexcraft/FCL) - Fexcraft Common Library
  - Utils & other Common stuff States is  making use of.
- [FSMM](https://github.com/Fexcraft/FSMM) - Fex's Small Money Mod
  - Account & Money (Currency) Management mod, which is in it's latest versions pretty well customisable.

### License
http://fexcraft.net/license?id=mods
### Discord
https://discord.gg/AkMAzaA
### Wiki
https://github.com/Fexcraft/States/wiki
