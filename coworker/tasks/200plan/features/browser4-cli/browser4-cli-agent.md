# 实现 browser4-cli agent 模式

## Agent Mode

```shell
# High-level agent commands
browser4-cli goto https://example.com/form
browser4-cli agent act "scroll to the middle"
browser4-cli agent summarize --prompt "Tell me about something about this page" --output=summary.txt
browser4-cli agent extract "product name, price, ratings" --output=product.json

browser4-cli agent run 'visit x.com, search for @galaxyeye8, and summarize the latest 5 posts'

browser4-cli agent run '
Go to https://www.amazon.com/dp/B08PP5MSVB

After browser launch:
  - clear browser cookies
  - go to https://www.amazon.com/
  - wait for 5 seconds
  - click the first product link
After page load: scroll to the middle.

Summarize the product.
Extract: product name, price, ratings.
Find all links containing /dp/.
'
```

## Implementation Notes

agent run 是长时操作，需要提供接口反馈和进度更新，以便用户了解任务的执行状态
