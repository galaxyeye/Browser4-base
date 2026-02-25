# 实现 browser4-cli 蜂群模式

## Swarm Mode

```shell
# Swarm mode: run multiple browser instances in parallel to perform tasks faster and more efficiently.
browser4-cli swarm create --profile-mode=temporary --max-open-tabs=8 --max-browser-contexts=2 --display-mode=GUI
browser4-cli swarm submit https://www.amazon.com/dp/B08PP5MSVB -deadline 2026-2-24T23:59:59Z
browser4-cli swarm submit --seed-file=seeds.txt
browser4-cli swarm scrape https://www.amazon.com/dp/B08PP5MSVB --selector=".product-title" --attribute="textContent" --output=title.txt

browser4-cli close
```
