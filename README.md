# Spring Security OTP Plugin

The Spring Security OTP plugin adds one-time password (OTP) functionality to applications using Spring Security.  One-time password tokens can be used as an out-of-band second factor of authentication.  This plugin supports transmitting OTP tokens via email and SMS text. 

## Components

### OtpGeneratingAuthenticationProvider

This wraps an existing `AuthenticationProvider`.  The authentication request is delegated to the wrapped provider's authenticate() method and the `Authentication` token returned (e.g. a `UsernamePasswordAuthenticationToken`)by this embedded provider is subsequently wrapped in a `PreOtpAuthenticationToken`.  This token contains the principal and details of the wrapped token, but does not expose its authorities.

If the embedded authentication provider authenticates the user successfully, the `OtpGeneratingAuthenticationProvider` will use its `Tokenstore`, `LookupStrategy` and `SendStrategy` to generate a one-time password token and attempt to send it to the user for entry on a subsequent form.

### OtpValidationFilter

This listens on a preconfigured endpoint for an OTP token sent by the user (from an OTP entry form).  If the user's token in the `SecurityContext` is a `PreOtpAuthenticationToken`, the filter will attempt to validate their OTP token from the input form against the `Tokenstore`.  If successful, the embedded token in the `PreOtpAuthenticationToken` will be unwrapped and they will be redirected to the success URL.  If unsuccessful, their token will be invalidated and they will be redirected to the failure URL.

### Tokenstore

Implementations of `Tokenstore` determine how OTP tokens are stored in the system (in memory, in a database, etc).  Current implementations included are, `LocalTokenstore` (**not** recommended for production use), `JdbcTokenstore`, and `RedisTokenstore`.  Custom implementations can be used for other scenarios, provided they conform to the `Tokenstore` contract.

### LookupStrategy

Implementations of `LookupStrategy` are used to lookup user contact information.  Information is returned as a `String`, and may consist of an email address, phone number, etc.  Included implementations are `LdapLookupStrategy` and `JdbcLookupStrategy`.

### SendStrategy

Implementations of `SendStrategy` define how OTP tokens are sent to users once they are generated.  Tokens may, for example, be sent to users over email or SMS text message.  Included implementations are `EmailSendStrategy` and `SmsSendStrategy`.