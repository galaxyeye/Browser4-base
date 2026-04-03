# Fix Encoding for Log Of Browser4.jar

When browser4-cli start Browser4.jar, it will create a log file named `pulsar.log` in the `sdks/browser4-cli` directory.

The log file contains garbled characters:

```
Example 1: ʹ�� Web UI (Using the Web UI):
```

[pulsar.log](../../../sdks/browser4-cli/logs/pulsar.log)
