# 鍐呯疆 AI 鍗忎綔鍔╂墜锛圔uiltin AI Coworker锛?
鍐呯疆 AI 鍗忎綔鍔╂墜鏄竴涓唬鐞嗭紙agent锛夛紝鍙崗鍔╀綘鍦ㄤ粨搴撲腑瀹屾垚鍚勭浠诲姟銆備綘鍙渶鍒涘缓浠诲姟鏂囦欢锛屽崗浣滃姪鎵嬩細澶勭悊杩欎簺鏂囦欢銆佹墽琛屼换鍔★紝骞跺彲灏嗘洿鏀规彁浜ゅ洖浠撳簱銆?
## 浣跨敤鏂规硶

鍚姩鍔╃悊 鈫?鎵归噺璧疯崏浠诲姟 鈫?澶嶅埗鍒版墽琛岀洰褰?鈫?鍔╂墜鎵ц浠诲姟 [ 鈫?鏌ョ湅缁撴灉 鈫?瀹℃煡 ] 鈫?绉诲姩鍒版壒鍑嗙洰褰?鈫?鑷姩鎻愪氦鎺ㄩ€?
1. 杩愯 `coworker-scheduler.ps1` 浠ュ惎鍔ㄥ畾鏃惰嚜鍔ㄥ寲
2. 鍦?`0draft` 涓嬭捣鑽変换鍔★紙鎴栬€呬换浣曞湴鏂癸級
3. 灏嗗凡瀹屾垚鑽夌鐨勪换鍔″鍒跺埌 `1created` 鐩綍浠ユ墽琛?4. 鎵ц鍚庯紝鎮ㄥ彲浠ュ湪 `3_1complete` 涓壘鍒扮粨鏋滐紝鍦?300logs 涓壘鍒拌缁嗘棩蹇?5. 濡傛湁闇€瑕侊紝澶嶆牳缁撴灉
6. 灏嗕换鍔℃枃浠朵粠 `3_1complete` 绉诲姩鍒?`5approved` 浠ヤ究瑙﹀彂 git 鎺ㄩ€?
## 宸ヤ綔娴佺▼

浠诲姟鏂囦欢浼氬湪 `coworker/tasks/` 鐩綍涓嬬殑缂栧彿鏂囦欢澶逛腑娴佽浆锛?
| 闃舵   | 鏂囦欢澶?        | 璇存槑                     |
|--------|----------------|--------------------------|
| 鑽夌   | `0draft`     | 鍦ㄦ澶勫垱寤哄拰缂栬緫浠诲姟鏂囦欢 |
| 闃熷垪   | `1created`     | 鍑嗗鎵ц鏃剁Щ鍏ユ鏂囦欢澶?  |
| 瑙勫垝   | `200plan`      | 浠ｇ悊瑙勫垝闃舵锛堣嚜鍔ㄧ鐞嗭級 |
| 鎵ц   | `2working`     | 浠ｇ悊姝ｅ湪鎵ц浠诲姟         |
| 瀹屾垚   | `3_1complete`    | 鎵ц缁撴潫锛屽彲瀹℃煡鏇存敼     |
| 瀹℃煡   | `4review`      | 鍙€夌殑浜哄伐瀹℃煡闃舵       |
| 宸叉壒鍑?| `5approved`    | 宸叉壒鍑嗕换鍔★紝绛夊緟鎻愪氦鎺ㄩ€?|
| 宸叉帹閫?| `6git-pushed`  | 宸叉垚鍔熸彁浜ゅ苟鎺ㄩ€?        |
| 褰掓。   | `700archive`   | 宸插綊妗ｇ殑宸插畬鎴愪换鍔?      |

## 蹇€熷紑濮?
1. **鑽夌** 鈥?鍦?`coworker/tasks/0draft/` 鍒涘缓浠诲姟鏂囦欢銆?2. **闃熷垪** 鈥?鍑嗗濂藉悗灏嗗叾绉昏嚦 `coworker/tasks/1created/`銆?3. **鎵ц** 鈥?杩愯鍗忎綔鍔╂墜鑴氭湰澶勭悊浠诲姟锛?   - Windows: `.\coworker\scripts\coworker.ps1`
   - Python: `python .\coworker\scripts\coworker.py`
   - Linux/macOS: `./coworker/scripts/coworker.sh`
   - Linux/macOS (Python): `python3 ./coworker/scripts/coworker.py`
4. **瀹℃煡** 鈥?浠诲姟鎵ц鍚庝細杩涘叆 `3_1complete`锛屽彲瀹℃煡鏇存敼銆?5. **鎵瑰噯** 鈥?灏嗕换鍔＄Щ鑷?`5approved`锛屽畾鏃朵换鍔′細鑷姩鎻愪氦骞舵帹閫併€?
## 鍓嶇疆鏉′欢

闇€瀹夎骞惰璇?GitHub CLI锛坄gh`锛夈€?
瀹夎鏂规硶璇﹁锛歨ttps://github.com/cli/cli#installation

## 鏍囩锛圱ags锛?
浣犲彲浠ュ湪浠诲姟鏂囦欢涓娇鐢ㄦ爣绛撅紝鎻愪緵棰濆涓婁笅鏂囨垨鎺у埗琛屼负銆?
鏀寔鐨勬爣绛撅細

- `#auto-approve` 鈥?浠诲姟瀹屾垚鍚庤嚜鍔ㄧЩ鑷?`5approved`锛屾棤闇€浜哄伐瀹℃煡锛岄€傜敤浜庝綆椋庨櫓銆佸彲淇′换鍔°€?
## 鎻愬強锛圡entions锛?
> **瀹為獙鎬у姛鑳?*

鍦ㄤ换鍔℃枃浠朵腑鎻愬強 `@coworker`锛屽彲閫氱煡浠ｇ悊澶勭悊璇ヤ换鍔°€?
## 涓?Git 鍚屾

浠诲姟鎵瑰噯鍚庯紝鍙娇鐢?git-sync 鑴氭湰灏嗘洿鏀规帹閫佸埌浠撳簱銆?
**Windows (PowerShell)锛?*

```powershell
.\coworker\scripts\workers\git-sync.ps1
```


**Linux/macOS (Bash)锛?*

```bash
./coworker/scripts/workers/git-sync.sh
```

## 缁熶竴璋冨害鍣紙PowerShell锛?
濡傛灉浣犲笇鏈涘彧閰嶇疆涓€涓?Windows Task Scheduler 瑙﹀彂鍣紝璇蜂娇鐢ㄧ粺涓€璋冨害鍣ㄣ€傚畠浼氭寜閰嶇疆鍒嗗埆鍚姩鍚勪釜 PowerShell 瀛愯繘绋嬶紝淇濆瓨 stdout/stderr 鏃ュ織锛屽苟鎸佺画鎶婁换鍔＄姸鎬佸啓鍏?`logs/scheduled-tasks.status.json`銆?
浠诲姟瀹氫箟浣嶄簬 `coworker/scripts/coworker-scheduler.config.psd1`銆傛瘡涓换鍔￠兘鍙互鐙珛鍚敤鎴栫鐢紝骞跺崟鐙缃?`IntervalSeconds`銆佽剼鏈矾寰勩€佸弬鏁般€佸彲閫夌殑 `DependsOn` 渚濊禆椤哄簭锛屼互鍙婂彲閫夌殑 `PendingPaths` 杈撳叆闃熷垪銆傞厤缃?`PendingPaths` 鍚庯紝璋冨害鍣ㄤ細鍏堟鏌ヨ繖浜涙枃浠?鐩綍涓槸鍚︾湡鐨勬湁寰呭鐞嗗唴瀹癸紝鍙湁瀛樺湪宸ヤ綔椤规椂鎵嶄細鍚姩鏂扮殑 PowerShell 瀛愯繘绋嬨€?
**Windows (PowerShell)锛?*

```powershell
.\coworker\scripts\coworker-scheduler.ps1
.\coworker\scripts\coworker-scheduler.ps1 -Once
```

榛樿璋冨害浠诲姟锛?
- `coworker` 鈥?鍦ㄤ换鍔℃簮鐩戞帶涔嬪悗澶勭悊鎺掗槦涓殑 coworker 浠诲姟
- `draft-refinement` 鈥?澶勭悊鑽夌娑﹁壊闃熷垪
- `process-task-source` 鈥?鍚敤鍚庤疆璇㈤厤缃殑浠诲姟婧愬苟鍒嗗彂鏂颁换鍔?
缁熶竴璋冨害鍣ㄤ細璋冪敤 `coworker/scripts/deprecated/` 涓繚鐣欑殑鏃х増涓€娆℃€у疄鐜般€傛洿娓呮櫚鐨?PowerShell 鍏ュ彛鍒嗗埆鏄?`coworker/scripts/process-coworker-queue.ps1`銆乣coworker/scripts/process-draft-refinement-queue.ps1` 鍜?`coworker/scripts/process-task-source.ps1`锛涙棫鐨?`run_*_periodically.ps1` 鍚嶇О浠嶄繚鐣欎负鍏煎鍖呰鍣紝骞朵細鍏堣緭鍑哄純鐢ㄨ鍛婂啀杞彂銆?
## 鏃х増闃熷垪澶勭悊鑴氭湰

濡傛灉浣犻渶瑕佺洿鎺ユ墽琛屼竴娆℃€ф垨寰幆寮忓鐞嗭紝璇蜂娇鐢ㄨ繖浜涙洿娓呮櫚鐨勬棫鐗堥槦鍒楀鐞嗚剼鏈細

- `coworker/scripts/process-coworker-queue.ps1`
- `coworker/scripts/process-draft-refinement-queue.ps1`
- `coworker/scripts/process-task-source.ps1`

缁熶竴璋冨害鍣ㄥ疄闄呰皟鐢ㄧ殑瀹炵幇浣嶄簬锛?
- `coworker/scripts/deprecated/process-coworker-queue.ps1`
- `coworker/scripts/deprecated/process-draft-refinement-queue.ps1`
- `coworker/scripts/deprecated/process-task-source.ps1`

涓轰簡鍏煎鏃ф祦绋嬶紝浠ヤ笅鏃у悕绉颁粛鐒跺彲鐢紝浣嗕細鎻愮ず寮冪敤锛?
- `coworker/scripts/run_coworker_periodically.ps1`
- `coworker/scripts/run_draft_refinement_periodically.ps1`

濡傛灉鏄畾鏃惰嚜鍔ㄥ寲锛岃浼樺厛浣跨敤 `coworker-scheduler.ps1`銆?

**Windows (PowerShell)锛?*

```powershell
.\coworker\scripts\process-coworker-queue.ps1
.\coworker\scripts\process-coworker-queue.ps1 -Once
```

## 鑽夌娑﹁壊

鑽夌娑﹁壊浣跨敤 `coworker/tasks/0draft/refine/` 涓嬬殑涓撶敤娴佺▼锛?
- `1ready` 鈥?绛夊緟娑﹁壊鐨勮崏绋?- `2working` 鈥?姝ｅ湪娑﹁壊鐨勮崏绋?- `3done` 鈥?宸插畬鎴愭鼎鑹层€佺瓑寰呭闃呯殑鑽夌

浣犲彲浠ユ鼎鑹插崟涓枃浠讹紝涔熷彲浠ヤ紶鍏ヤ竴涓枃浠跺す鎵归噺澶勭悊锛涗紶鍏ユ枃浠跺す鏃朵細閫愪釜鏂囦欢鎵ц銆?
**Windows (PowerShell)锛?*

```powershell
.\coworker\scripts\workers\refine-drafts.ps1
.\coworker\scripts\workers\refine-drafts.ps1 -Path .\coworker\tasks\0draft\refine\1ready
.\coworker\scripts\coworker-scheduler.ps1
```

**Linux/macOS (Bash)锛?*

```bash
./coworker/scripts/workers/refine-drafts.sh
./coworker/scripts/workers/refine-drafts.sh ./coworker/tasks/0draft/refine/1ready
pwsh ./coworker/scripts/process-draft-refinement-queue.ps1 -Once
```


