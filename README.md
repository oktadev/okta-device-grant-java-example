# OAuth 2.0 Device Grant w/ Java and JBang

This example shows how to create a CLI Java application that uses the OAuth 2.0 Device Authorization Grant (login with a code).

Please read [Authenticate from the Command Line with Java][blog-url] for a tutorial that shows you how to build this application.

**Prerequisites:**
* An Okta organization - (Sign up for free using your [browser](https://developer.okta.com/signup/), or the [Okta CLI](https://cli.okta.com/))
* [Java 11+](https://adoptium.net/) (Tested with 11, 17, & 18)
* [JBang](https://www.jbang.dev/download/) (v0.92.2)

**Table of Contents**
* [Getting Started](#getting-started)
* [Help](#help)
* [Links](#links)
* [License](#license)

## Getting Started

Clone the project:

```bash
git clone https://github.com/oktadev/okta-device-grant-java-example.git
cd okta-device-grant-java-example
```

## Configure the application

See the [blog post][blog-url] for detailed descriptions on how to create an Okta application and locate the Client ID and Issuer.
Update the issuer in `DeviceGrant.java`

### Run the application

To start the application you can run:

```bash
jbang DeviceGrant.java
```

## Links

This example uses the following libraries:

* [Jackson Databind](https://github.com/FasterXML/jackson-databind)
* [JBang](https://www.jbang.dev/)

## Help

Please post any questions as comments on [this blog post][blog-url], or visit our [Okta Developer Forums](https://devforum.okta.com/). You can also email developers@okta.com if would like to create a support ticket.

## License

Apache 2.0, see [LICENSE](LICENSE).

[blog-url]: https://developer.okta.com/blog/2022/04/11/java-cli-device-grant
