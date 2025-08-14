
# AI Dating Assistant Telegram Bot

A Telegram bot that acts as an intelligent dating assistant powered by ChatGPT. The bot helps users with various aspects of online dating, from profile optimization to messaging strategies.

## Features

- **Profile Generation** (`/profile`) - Get AI assistance in creating an attractive and effective dating profile
- **Conversation Opener** (`/opener`) - Generate personalized opening messages based on match information
- **Smart Messaging** (`/message`) - Get AI suggestions for continuing conversations and moving them forward
- **Celebrity Chat Simulation** (`/date`) - Practice dating conversations by chatting with AI simulating celebrities like:
  - Ariana Grande
  - Margot Robbie
  - Zendaya
  - Ryan Gosling
  - Tom Hardy
- **AI Assistant** (`/gpt`) - Get general dating advice and tips from ChatGPT

## Technical Stack

- Java 17
- Jakarta EE
- Telegram Bots API
- OpenAI ChatGPT API
- Lombok
- SLF4J for logging

## Getting Started

1. Clone the repository
2. Configure the following environment variables or update them in `TinderBoltApp.java`:
   - `TELEGRAM_BOT_TOKEN` - Your Telegram bot token from BotFather
   - `TELEGRAM_BOT_NAME` - Your bot's username
   - `OPEN_AI_TOKEN` - Your OpenAI API key

## Usage

1. Start a chat with the bot on Telegram
2. Use `/start` to see the main menu
3. Choose from available commands:
   - `/profile` - Create your dating profile
   - `/opener` - Generate conversation starters
   - `/message` - Get messaging assistance
   - `/date` - Practice with celebrity chat simulations
   - `/gpt` - Get general dating advice
   - `/stop` - End current session

## Building and Running
bash ./mvn clean install java -jar target/tinder-bot.jar

## Note

Please ensure you have valid API keys for both Telegram and OpenAI before running the bot.

## Security Notice

Remember to keep your API tokens secure and never commit them directly to version control.
