# Spring Security OTP Plugin

The Spring Security OTP plugin adds one-time password (OTP) functionality to applications using Spring Security.  One-time password tokens can be used as an out-of-band second factor of authentication.  This plugin supports transmitting OTP tokens via email and SMS text. 

## Components

### OtpGenerationFilter

This filter behaves as the endpoint for token generation requests.  Usually, these requests will come in as AJAX calls from the login page.  When this filter receives a username from the an HTTP call, it uses the `LookupStrategy` injected into it to determine where (email address, phone number, etc) the `SendStrategy` should send the generated OTP token.  The generated token is stored in the filter's assigned `Tokenstore`.

### OtpAuthenticationFilter

This filter extends the functionality of Spring Security's `UsernamePasswordAuthenticationFilter` by adding a required OTP token parameter to incoming requests.  When it receives an authentication request, it first checks the `Tokenstore` to verify that the OTP token on the request is valid.  If so, the filter continues by attempting to verify the username and password.  If either the username/password or OTP token check fail, an `AuthenticationException` will be thrown.

### Tokenstore

Implementations of `Tokenstore` determine how OTP tokens are stored in the system (in memory, in a database, etc).  Current implementations included are, `LocalTokenstore` (**not** recommended for production use), `JdbcTokenstore`, and `RedisTokenstore`.  Custom implementations can be used for other scenarios, provided they conform to the `Tokenstore` contract.

### LookupStrategy

Implementations of `LookupStrategy` are used to lookup user contact information.  Information is returned as a `String`, and may consist of an email address, phone number, etc.  Included implementations are `LdapLookupStrategy` and `JdbcLookupStrategy`.

### SendStrategy

Implementations of `SendStrategy` define how OTP tokens are sent to users once they are generated.  Tokens may, for example, be sent to users over email or SMS text message.  Included implementations are `EmailSendStrategy` and `SmsSendStrategy`.