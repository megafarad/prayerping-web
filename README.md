# PrayerPing

**PrayerPing** is an interfaith prayer request app that allows users to share and respond to prayer requests within a community. This app is built with scalability and privacy in mind, supporting multiple faith traditions and offering a respectful space for spiritual support.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Features

- **Prayer Requests:** Users can post prayer requests and receive support from the community.
- **Interfaith Support:** Built to accommodate multiple faith traditions with sensitivity.
- **User Profiles:** Users can create profiles, follow others, and engage with their requests.
- **Notifications:** Get notified when someone responds to your prayer request.
- **Privacy:** User data is kept private, and the app follows strict data security guidelines.

## Tech Stack

- **Back-End:** Scala Play Framework
- **Front-End:** React with Redux (using Redux Toolkit)
- **Database:** PostgreSQL
- **Authentication:** Play Silhouette
- **WebSockets Integration:** Real-time updates via WebSocket

## Installation

### Prerequisites

- **Scala** (2.13 or later)
- **SBT** (Scala Build Tool)
- **Node.js** (14.x or later)
- **PostgreSQL** (12.x or later)

### Steps

1. **Clone the repository:**

   ```bash
   git clone https://github.com/megafarad/prayerping-web.git
   cd prayerping-web
   ```

2. **Set up the database:**

    - Create a PostgreSQL database named `prayerping`.
    - Update the `application.conf` file in the `conf` directory with your database credentials.

3. **Install dependencies:**

   ```bash
   sbt update
   cd frontend
   npm install
   ```

4. **Run the application:**

   ```bash
   sbt run
   ```

   The application should now be running on `http://localhost:9000`.

## Usage

Once the app is running, you can:

- Register and create a profile.
- Post, view, and respond to prayer requests.
- Follow other users to keep track of their requests.
- Customize your notification preferences.

## Contributing

Contributions are welcome! Here's how you can help:

1. **Fork the repository**
2. **Create a new branch** (`git checkout -b feature/YourFeature`)
3. **Commit your changes** (`git commit -m 'Add some feature'`)
4. **Push to the branch** (`git push origin feature/YourFeature`)
5. **Open a Pull Request**

Please make sure your code follows the project's coding standards and includes tests where applicable.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

If you have any questions or feedback, feel free to reach out:

- **Project Maintainer:** [Your Name](mailto:your.email@example.com)
- **GitHub Issues:** [https://github.com/yourusername/PrayerPing/issues](https://github.com/yourusername/PrayerPing/issues)

---

This template should give you a solid foundation for the README file. If you have any specific sections you'd like to expand on or additional information you'd like to include, feel free to modify it!