### TODO LIST for v.1.1:

in order of priority

* **Resource:** Create and upload a video demonstration of the plugin.
---
* **Bugfix:** There's a strange bug where using the "/bt get" command in survival mode
does not genereate any banners.
---
* **Bugfix:** The semicolon ';' character cannot be used with the BannerText writer because it
gets confused with the other semicolons used as delimeters in the writer's lore data.
---
* **Typo:** The plugin's info page displays the help command as being "/bt help 1"-- this can be
changed to "/bt help"
---
* **Feature:** There's currently no limit to the length of text entered with "/bt get <text...>", 
Filling the whole chat bar with text took 400ms. Isn't a huge concern since the permission
for this command defaults to OP, but the potential for abuse is still there.
---
* **Feature:** Add a cool-down to limit the amount of resources each player can consume
per unit time. Not important unless server admins want to extend plugin permissions 
to potentially untrustworthy players.
