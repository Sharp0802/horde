# Horde

A library-first and intuitive forge mod to create horde wave.

## Runtime Command

- `/horde reload` : Reloads schedule data
- `/horde dump` : Dumps current schedule data
- `/horde run <name>` : Force-run a specified schedule
- `/horde tick` : Gets current tick count

## Configuration

`config/horde/default.json` in your minecraft folder is default Schedule Data file.
And it's loaded when `common_setup` phase of forge loader.

### Schedule Data File

```json
[
  ...
]
```

Schedule Data File is basically array of Schedule Object

#### Example

```json
[
  {
    "name": "daily-horde",
    "when": [
      {
        "type": "cyclic",
        "cycle": "5h",
        "offset": "0"
      }
    ],
    "spawn": [
      "daily-horde.csv"
    ]
  }
]
```

### Schedule Object

```json
{
  "name": "",
  "when": [],
  "spawn": []
}
```

- `name` (string) : Name of the schedule
- `when` (When[]) : Array of conditions; Schedule will be executed only if all conditions are satisfied
- `spawn` (string[]) : Array of spawn file path (all paths are relative to `config/horde`)

#### Example

```json
{
  "name": "daily-horde",
  "when": [
    {
      "type": "cyclic",
      "cycle": "5h",
      "offset": "0"
    }
  ],
  "spawn": [
    "daily-horde.csv"
  ]
}
```

### When Object

```json
{
  "type": "",
  ...
}
```

When Object represents when schedule should be executed

- `type` (string) : Type of when object; See below

#### Cyclic-When Object

```json
{
  "type": "cyclic",
  "cycle": "",
  "offset": ""
}
```

- `cycle` (Time String) : Cycle of signal
- `offset` (Time String) : Offset of signal

#### Once-When Object

```json
{
  "type": "once",
  "offset": ""
}
```

- `offset` (Time String) : Offset of signal

#### Example

```json
{
  "type": "cyclic",
  "cycle": "5h",
  "offset": "0"
}
```

### Spawn File Format

```csv
<entity>,<N>[,<P>[,<M>]]
```

- `entity` : Name of entity (such as `minecraft:pig`)
- `N` : Number *to* spawn
- `P` (Probability String) : Probability to spawn
- `M` : Number *of* spawn

With a probability of `P`, `N` creatures are summoned.
This process is repeated `M` times.

#### Example

```
minecraft:pig,2
minecraft:pig,2,50%,4
```

### Time String

```
<value>[<suffix>]

1d
24h
24000
```

- `value` : Numeric value
- `suffix` : Unit suffix (optional)

Available suffix is as follows:

- `d` : day (in minecraft time)
- `h` : hour (in minecraft time)

If no suffix is specified, value represents tick count.

### Probability String

```
<value>[<suffix>]
```

- `value` : Numeric value
- `suffix` : Unit suffix (optional)

Available suffix is as follows:

- `%` : percent; value should be in \[0, 100\]

If no suffix is specified, value should be in \[0, 1\].

## Configuration with API

- `Config#loadSchedule` (`com.sharp0802.horde`) : Load custom schedule data file
- `Config#reset` : Unload all schedule data
