# Fix bugs

The following tests in `ai.platon.pulsar.rest.api.controller.MCPToolControllerE2ETest` are failed, fix them:

```
Calling an unknown tool returns an error
Calling a tool without sessionId returns an error
Calling a tool with an invalid sessionId returns an error
close_session returns error for unknown sessionId
delete_session_data clears cookies and storage (cli: delete-data)
```
