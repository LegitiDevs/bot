First install MongoDB and run it. Then create a [discord bot](https://discord.com/developers/applications) for the bot to use and save the token for later

Create a webhook on the channel you want to use for incoming messages from the minecraft server. Then, rename `run/config/legitimoosebot-config.properties.example` to `legitimoosebot-config.properties` and enter the bot token, webhook url (you can safely ignore errorWebhookUrl), and the channel ID of your bot's channel.

You can now launch the bot from your IDE and test your changes locally. You can use the `/scrape` command to manually trigger a world scrape while on moose.