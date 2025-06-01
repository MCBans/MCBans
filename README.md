MCBans Server Plugin

Author: Firestarthe, Syamn, Corpdraco


Welcome to MCBans
====================

MCBans is a global banning solution for Minecraft, provided through the Bukkit plugin. The aim of the plugin is to provide server owners with a method of assisting in the prevention of griefers on their own and other's servers, using both local and global bans.

Helpful Information
--------
Main Site: https://mcbans.com

Support Desk: https://forums.mcbans.com/support-tickets/open

Community: https://forums.mcbans.com

Expectations
--------

Please remember the following things while using our plugin!

* MCBans is maintained constantly by a team of staff, but we're not miracle workers! Like everyone, we need time to perform our tasks and can't do everything on demand.
* MCBans isn't always 100% clear from invalid bans - but, we're well on our way. Your help is always thanked in removing invalid bans and we try our hardest to monitor everything.
* MCBans has a small dedicated development team - We do only have a few dedicated developers, but they do have other commitments as well.
* MCBans staff don't always have good memories! Visit our support desk.

Commands
--------

Local Ban Variations
* /ban <playername|UUID> <reason> - bans the player
* /rban <playername|UUID> <reason> - rollback and ban

E.G. /ban Firestarthe breaking my sign

Global Ban Variations
* /ban <playername|UUID> g <reason> - global ban
* /gban <playername|UUID> <reason> - global ban
* /rban <playername|UUID> g <reason> - rollback and global ban

E.G. /ban g Firestarthe griefing

Temporary Ban Variations
* /ban <playername|UUID> t <int> <m/h/d> <reason> - temp bans a player
* /tban <playername|UUID> <int> <m/h/d> <reason> - temp bans a player
* /rban <playername|UUID> t <int> <m/h/d> <reason> - temp bans a player and rolls them back

E.G. /tban Firestarthe 15 m you are banned for 15 minutes

IP Ban Variations
* /banip <IP> [reason] - bans an IP address

Misc Commands
* /lookup <playername|UUID> - checks players ban history (local/global bans)
* /banlookup <banID> - checks ban details
* /altlookup <playername|UUID> - checks alt accounts (premium server only!)
* /kick <playername|UUID> [reason] - kicks a player from the game
* /unban <playername|IP|UUID> - unbans the player/IP from your server
* /mcbans - mcbans help and more information
* /mcbs - change server settings

Permissions
--------

* mcbans.admin (default: op) - Admin privileges to mcbans
* mcbans.ban.global (default: op) - Allow global ban player
* mcbans.ban.local (default: op) - Allow local ban player
* mcbans.ban.temp (default: op) - Allow temp ban player
* mcbans.ban.rollback (default: op) - Allow use rban command
* mcbans.ban.ip (default: op) - Allow use banip command
* mcbans.ban.exempt (default: op) - Permission to exempt from bans
* mcbans.unban (default: op) - Allow unban player
* mcbans.view.alts (default: op) - Show notification of a players alts on connect
* mcbans.view.bans (default: op) - Show previous ban information on player connect
* mcbans.view.staff (default: true) - Show notification of a mcbans staff on connect
* mcbans.hideview (default: false) - Hide player view alts/previous bans on connect
* mcbans.lookup.player (default: op) - Allow lookup player ban history
* mcbans.lookup.ban (default: op) - Allow lookup ban details
* mcbans.lookup.alt (default: op) - Allow lookup alt account
* mcbans.kick (default: op) - Allow kick player
* mcbans.kick.exempt (default: op) - Permission to exempt from kicks
* mcbans.maxalts.exempt (default: op) - Permission to exempt from max alt account disconnect




Docker Images and Kubernetes
--------

This project includes Docker images for testing the MCBans API:

1. **mcbans-main**: A simple test that connects to the MCBans API and verifies the connection
2. **mcbans-bantest**: A more comprehensive test that bans a player, checks the ban status, and then unbans the player

### Running with Kubernetes

A Kubernetes CronJob configuration is provided in `kubernetes-jobs.yaml`. This file defines two CronJobs that run every hour:
- `mcbans-main-cronjob`: Runs the Main class to test API connectivity every hour
- `mcbans-bantest-cronjob`: Runs the BanTest class to test ban functionality every hour

To apply the configuration to your Kubernetes cluster:

```bash
# Apply the configuration
kubectl apply -f kubernetes-jobs.yaml

# Check the status of the CronJobs
kubectl get cronjobs

# Check the status of the Jobs created by the CronJobs
kubectl get jobs

# View logs from a job pod (replace JOB_POD_NAME with the actual pod name)
kubectl logs JOB_POD_NAME

# Delete the CronJobs when done
kubectl delete -f kubernetes-jobs.yaml
```

You can customize the API key and other parameters by editing the `args` section in the YAML file before applying. You can also modify the schedule by changing the `schedule` field in the CronJob specification (default is "0 * * * *", which runs at minute 0 of every hour).
