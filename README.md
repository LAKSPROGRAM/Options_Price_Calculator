# 📈 Options Price Calculator & Predictor

A full-stack algorithmic trading utility designed to fetch live market data, compute Black-Scholes Greeks, and predict future option premiums for Nifty 50 contracts using real-time data from the Upstox API.

## ✨ Key Features

⚡ Live Market Data: Fetches real time spot prices, option premiums, and volume directly from the Upstox API v2.
🧮 Advanced Options Math: Calculates Implied Volatility (IV) using the Newton-Raphson method and derives all five Black-Scholes Greeks (Delta, Gamma, Theta, Vega, Rho).
🔮 Target Scenario Prediction: Predicts future option premiums using Taylor Expansion based on user-defined target Nifty levels and time decay (minutes to target).
🎯 Smart Instrument Locator: Automatically parses Upstox CSV master files to locate the correct trading symbol and instrument key for the current trading day.
🛡️ Robust Error Handling: Gracefully handles API rate limits, expired access tokens, and invalid instrument keys.

 🛠️ Tech Stack
 Backend
Java 17 + Spring Boot 3.2 (Web, WebFlux for reactive API calls)
Apache Commons Math3 (For Normal Distribution CDF/PDF modeling)
Lombok (Boilerplate reduction)
Maven (Dependency management)

 Frontend
HTML5 / CSS3 / Vanilla JavaScript**
Snake_case JSON mapping for seamless communication with the Spring Boot backend.

---

⚙️ Installation & Setup

Prerequisites
1. Java Development Kit (JDK) 17 or higher.
2. Apache Maven installed.
3. An active [Upstox Developer Account](https://developer.upstox.com/) to generate API Access Tokens.

### 1. Clone the Repository
```bash
git clone [https://github.com/LAKSPROGRAM/Options_Price_Calculator.git](https://github.com/LAKSPROGRAM/Options_Price_Calculator.git)
cd Options_Price_Calculator
