# States
Hopefully less issueful successor of the "Nations" Series.

### So... what is "States"?
Similarly to "Nations" (an old FSU module), it has a multi-layer protection system.
In the case of "States", the world is composed of `Chunk -> District -> Municipality -> State`.
Players can create a "Municipality", which depending on (configurable!) size will be called "Hamlet", "Village", "Town" and so on.
The Size also decides how many districts a Municipality can have. There is also the "Village" district type for sub-villages.<br>
Here an example Image:
![STCM](https://i.imgur.com/6KgjMei.png)<br>
Chunks also can have different types (based on which their access and protection do work) & be linked to each other, meaning that if the "core" chunks changes the owner, the linked ones do aswel.
![STPM](https://i.imgur.com/pDfBGTV.png)
<br>
By default access the "wilderness" (unclaimed land) is disabled, so players cannot edit blocks there, which can be toggled off in config.
Players can in such cases for example claim "temporarily" chunks, which are called "Transit Zones", such claims are valid for 1 day, and for example useful to build roads to Municipalities which aren't directly connected.
<br>
There are also other fancy additional features like "Sign Shops", which can be directly setup to pay/take money from the Municipality's or State's account, besides being able to use them as Private (Player) Shops.
[SignShop](https://cdn.discordapp.com/attachments/424351061873131521/443900271362572288/2018-05-10_00-17-07.mp4) (VIDEO)

### Other mentionable things:
- a GUI for Claiming Land
- a GUI to see the whole Region the Player is on at once (32x32 chunks area),
- - available modes being "terrain", "claims", "districts", "municipalities", "states"
- every district/municipality/state can have a custom (RGB/HEX) color on map! (actually suggested to do due to the map)
- districts have own "types", based on which (when `Companies` will be added) buying of terrain may be limited under specific circumstances, to be able to create e.g. "commercial", "industrial", "residental", "agricultural", etc districts in municipalities.
- notification (GUI) upon entering a different area, be it another District, Municipality or State!
- Discord Chat integration (currently requires usage of my DiscordBot to send messages back)
- _States_ uses the FSMM Currency/Account Management, FSMM also allows to setup items from other mods as Money, disable the default FSMM ones, or generate more.
- and some more things!
<br>
If you want to help out, be it in actual code, assets or in pure theorizing on best settings and new features, feel free to join our Discord server! (Depending on mood new Ideas might not be welcomed instantly :grin: )
<br>
<b>This Mod does not contain a "war" mode, nor are there any plans to add one, ever.</b>
As some years of server management experience confirm, it's for one hard to have a realistic war in MC, and for other part, if allowing it to be "minecrafty", it will be a mere clicking around with swords, which I do not like.
There are also many other reasons why I won't do it, available on request.

### Why "States" as name? Wasn't the old one good enough?
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
Considering I always never minded having members from various countries and cultures, as it for one sounds better,
and for the other from experience I know people too often ended up only trying to annihilate each other, militarizing,
and at end getting mad at me that I didn't allow wars/terrain destruction. **¯\\\_(ツ)_/¯**

### So no way to hold advantage over others, or annoy hostile people?
Economy, _economy_.
"Companies" will be also a feature which will be added in the future.<br>
And you will be able to setup tolls or banish companies from other States.<br>
Additionally if you would mix up some nice survival mods into a Server/Modpack with a large Map and keep wilderness-access off (like e.g. I do), it can become a nice challenging gameplay, as people will need to trade some resources to get further, well, at least in theory.

### Why do you try so hard in keeping it war-free/peaceful?
Uh, why not? Seen the real world? It's a mess, cannot expect it will be perfect in MC,
as Human's aren't adapted to rule over other humans, but that doesn't mean our free time has to be as miserable.

### Anything else to know?
_**Ask**, as I have partial memory loss, and already forgot some of things I wanted to write, I think?_

### Depencencies
- [FCL](https://github.com/Fexcraft/FCL) - Fexcraft Common Library<br>
Utils & other Common stuff States is  making use of.
- [FSMM](https://github.com/Fexcraft/FSMM) - Fex's Small Money Mod<br>
Account & Money(Currency) Management mod, which is in it's latest versions pretty well customisable.

### License
http://fexcraft.net/license?id=mods
### Discord
https://discord.gg/rMXcrsv
### Wiki
https://github.com/Fexcraft/States/wiki (we may need help filling that one...)
