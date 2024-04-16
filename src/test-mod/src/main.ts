const BED_REGEX = /^minecraft:\w+_bed$/;

function log(...args: any[]) {
    stdout.write(Encoding.encode(args.join(' ') + '\n'));
}

interface GenItem {
    item: ItemStack;
    freq: number;
}

interface Gen {
    loc: Location;
    items: GenItem[];
}

interface Team {
    name: string;
    spawn: Location;
    bed: Location;
    gens: Gen[];
}

interface ShopItem {
    type: 'item';
    price: ItemStack;
    item: ItemStack;
}
interface ShopTier {
    type: 'tier';
    price: ItemStack;
    icon: ItemStack | number;
    tier: string;
    i: number;
}
interface ShopUpgrade {
    type: 'upgrade';
    price: ItemStack;
    icon: ItemStack;
    tier: string;
}

type ShopBuyable = ShopItem | ShopTier;

interface ShopCategory {
    icon: ItemStack;
    items: (ShopBuyable | undefined)[];
}

type Hotshop = Array<[number, number] | []>;

interface ToolTier {
    id: string;
    type: 'tool';
    forceFirst?: boolean;

    tiers: {
        item: ItemStack;
        multiple?: boolean;
        onDeath?: 'reset' | 'decrease' | number;
    }[];
}
interface ArmorTier {
    id: string;
    type: 'armor';
    forceFirst?: boolean;

    tiers: {
        helmet?: ItemStack;
        chestplate?: ItemStack;
        leggings?: ItemStack;
        boots?: ItemStack;
        onDeath?: 'reset' | 'decrease' | number;
    }[];
}
interface EnchantTier {
    id: string;
    type: 'enchant';
    forceFirst?: boolean;

    tiers: {
        targets: string[];
        enchants: Record<string, number>;
        onDeath?: 'reset' | 'decrease' | number;
    }[];
}

type Tier = ToolTier | ArmorTier | EnchantTier;

class Config {
    public readonly teams: Team[] = [
        {
            name: 'red',
            spawn: new Location(25, 20, 0),
            bed: new Location(20, 20, 0),
            gens: [
                { loc: new Location(30.5, 20, 0.5), items: [
                    { freq: 1.25, item: { id: 'iron_ingot', count: 1 } },
                    { freq: 5, item: { id: 'gold_ingot', count: 1 } }
                ] },
            ]
        },
        {
            name: 'blue',
            spawn: new Location(-25, 20, 0),
            bed: new Location(-20, 20, 0),
            gens: [
                { loc: new Location(-29.5, 20, 0.5), items: [
                    { freq: 1.25, item: { id: 'iron_ingot', count: 1 } },
                    { freq: 5, item: { id: 'gold_ingot', count: 1 } }
                ] },
            ]
        },
    ];
    public readonly gens: Gen[] = [
        { loc: new Location(0.5, 20, 0.5), items: [ { freq: 30, item: { id: 'emerald', count: 1 } } ] },
    ];
    public readonly countdowns = [
        { coef: .5, time: -1 },
        { coef: .75, time: 60 },
        { coef: 1, time: 10 },
    ];
    public readonly shop = {
        categories: [
            {
                icon: { id: 'white_wool', count: 1, display: { Name: '"Blocks"' } },
                items: [
                    { type: 'item', price: { id: 'iron_ingot', count: 4 }, item: { id: 'white_wool', count: 16 } },
                    { type: 'item', price: { id: 'iron_ingot', count: 12 }, item: { id: 'endstone', count: 12 } },
                    { type: 'item', price: { id: 'gold_ingot', count: 4 }, item: { id: 'oak_planks', count: 16 } },
                    { type: 'item', price: { id: 'emerald', count: 4 }, item: { id: 'obsidian', count: 4 } },
                ]
            },
            {
                icon: { id: 'iron_pickaxe', count: 1, display: { Name: '"Tools"' } },
                items: [
                    { type: 'tier', price: { id: 'iron_ingot', count: 10 }, tier: 'pickaxe', i: 0 },
                    { type: 'tier', price: { id: 'iron_ingot', count: 16 }, tier: 'pickaxe', i: 1 },
                    { type: 'tier', price: { id: 'gold_ingot', count: 8 }, tier: 'pickaxe', i: 2 },
                    { type: 'tier', price: { id: 'gold_ingot', count: 12 }, tier: 'pickaxe', i: 3 },
                    { type: 'tier', price: { id: 'iron_ingot', count: 10 }, tier: 'axe', i: 0 },
                    { type: 'tier', price: { id: 'iron_ingot', count: 16 }, tier: 'axe', i: 1 },
                    { type: 'tier', price: { id: 'gold_ingot', count: 8 }, tier: 'axe', i: 2 },
                    { type: 'tier', price: { id: 'gold_ingot', count: 12 }, tier: 'axe', i: 3 },
                    { type: 'tier', price: { id: 'iron_ingot', count: 40 }, tier: 'shears', i: 0 },
                ]
            },
            {
                icon: { id: 'iron_chestplate', count: 1, display: { Name: '"Armor"' } },
                items: [
                    { type: 'tier', price: { id: 'gold_ingot', count: 12 }, tier: 'armor', i: 1, icon: 2 },
                    { type: 'tier', price: { id: 'emerald', count: 6 }, tier: 'armor', i: 2, icon: 2 },
                ]
            },
            {
                icon: { id: 'iron_sword', count: 1, display: { Name: '"Weapons"' } },
                items: [
                    { type: 'tier', price: { id: 'iron_ingot', count: 10 }, tier: 'sword', i: 1 },
                    { type: 'tier', price: { id: 'gold_ingot', count: 7 }, tier: 'sword', i: 2 },
                    { type: 'tier', price: { id: 'emerald', count: 4 }, tier: 'sword', i: 3 },
                ]
            },
            {
                icon: { id: 'golden_apple', count: 1, display: { Name: '"Utilities"' } },
                items: [
                    { type: 'item', price: { id: 'iron_ingot', count: 10 }, item: { id: 'golden_apple', count: 16 } },
                    { type: 'tier', price: { id: 'emerald', count: 4 }, tier: 'elytra', i: 0, icon: 2 },
                    { type: 'item', price: { id: 'emerald', count: 10 }, item: { id: 'firework_rocket', count: 1 } },
                ]
            },
        ] as ShopCategory[],
        defaultHotshop: [
            [ 0, 0 ], [ 0, 1 ]
        ] as [number, number][]
    };

    public readonly tiers: Tier[] = [
        {
            id: 'armor',
            type: 'armor',
            forceFirst: true,
            tiers: [
                {
                    helmet: { id: 'minecraft:leather_helmet', count: 1 },
                    chestplate: { id: 'minecraft:leather_chestplate', count: 1 },
                    boots: { id: 'minecraft:leather_boots', count: 1 },
                    leggings: { id: 'minecraft:leather_leggings', count: 1 },
                },
                {
                    helmet: { id: 'minecraft:iron_helmet', count: 1 },
                    chestplate: { id: 'minecraft:iron_chestplate', count: 1 },
                    boots: { id: 'minecraft:iron_boots', count: 1 },
                    leggings: { id: 'minecraft:iron_leggings', count: 1 },
                },
                {
                    helmet: { id: 'minecraft:diamond_helmet', count: 1 },
                    chestplate: { id: 'minecraft:diamond_chestplate', count: 1 },
                    boots: { id: 'minecraft:diamond_boots', count: 1 },
                    leggings: { id: 'minecraft:diamond_leggings', count: 1 },
                },
            ]
        },
        {
            id: 'elytra',
            type: 'armor',
            tiers: [
                {
                    chestplate: { id: 'minecraft:elytra', count: 1 },
                }
            ]
        },
        {
            id: 'shears',
            type: 'tool',
            tiers: [
                { item: { id: 'minecraft:shears', count: 1 }, multiple: false },
            ]
        },
        {
            id: 'sword',
            type: 'tool',
            forceFirst: true,
            tiers: [
                { item: { id: 'minecraft:wooden_sword', count: 1 } },
                { item: { id: 'minecraft:stone_sword', count: 1 }, onDeath: "reset", multiple: true },
                { item: { id: 'minecraft:iron_sword', count: 1 }, onDeath: "reset", multiple: true },
                { item: { id: 'minecraft:diamond_sword', count: 1 }, onDeath: "reset", multiple: true },
            ]
        },
        {
            id: 'pickaxe',
            type: 'tool',
            tiers: [
                { item: { id: 'minecraft:wooden_pickaxe', count: 1 } },
                { item: { id: 'minecraft:stone_pickaxe', count: 1 }, onDeath: "decrease", multiple: true },
                { item: { id: 'minecraft:iron_pickaxe', count: 1 }, onDeath: "decrease", multiple: true },
                { item: { id: 'minecraft:diamond_pickaxe', count: 1 }, onDeath: "decrease", multiple: true },
            ]
        },
        {
            id: 'axe',
            type: 'tool',
            tiers: [
                { item: { id: 'minecraft:wooden_axe', count: 1 } },
                { item: { id: 'minecraft:stone_axe', count: 1 }, onDeath: "decrease", multiple: true },
                { item: { id: 'minecraft:iron_axe', count: 1 }, onDeath: "decrease", multiple: true },
                { item: { id: 'minecraft:diamond_axe', count: 1 }, onDeath: "decrease", multiple: true },
            ]
        },
    ];

    public readonly playerTiers = [ 'sword', 'armor', 'shears', 'pickaxe', 'axe', 'elytra' ];
    public readonly teamTiers = [ ];

    public readonly teamSize = 1;
    public readonly spawn = new Location(0, 50, 0);
    public readonly respawnTimeout = 5;
    public readonly yKillPlane = -20;
}

enum State {
    Alive = 'alive',
    Dead = 'dead',
    Eliminated = 'elim',
    Spectator = 'spec',
}

namespace InvUtils {
    export function equal(a: Item | undefined, b: Item | undefined) {
        if (a?.id === 'minecraft:air' || a?.id === 'air') a = undefined;
        if (b?.id === 'minecraft:air' || b?.id === 'air') b = undefined;

        if (a === b) return true;
        if (a == null || b == null) return a == null && b == null;

        a = { ...a };
        b = { ...b };

        if (!a.id.includes(':')) a.id = 'minecraft:' + a.id;
        if (!b.id.includes(':')) b.id = 'minecraft:' + b.id;

        if (a.id === b.id) return true;

        return false;
    }
    export function give(inv: Inventory, item: ItemStack): number {
        if (item.count < 0) return -take(inv, { ...item, count: -item.count });

        const maxCount =  Server.maxStack(item.id);
        const invItems: ItemStack[] = [];
        for (let i = 0; i < 36; i++) invItems[i] = inv.get(i);

        const giveCandidates = [
            invItems
                .map((v, i) => ({ v, i }))
                .filter(({v}) => equal(v, item))
                .sort((a, b) => a.v.count - b.v.count),
            invItems
                .map((v, i) => ({ v, i }))
                .filter(({v}) => equal(v, undefined))
        ].flat();

        let remaining = item.count;

        for (let { i, v } of giveCandidates) {
            const addItem = equal(v, { id: 'air' }) ? item : v;
            const count = v?.count ?? 0;

            if (remaining > maxCount - count) {
                inv.set(i, { ...addItem, count: maxCount });
                remaining -= maxCount - count;
            }
            else {
                inv.set(i, { ...addItem, count: remaining + count });
                remaining = 0;
                break;
            }
        }

        return remaining;
    }

    export function take(inv: Inventory, item: ItemStack): number {
        if (item.count < 0) return -give(inv, { ...item, count: -item.count });

        const invItems: ItemStack[] = [];
        for (let i = 0; i < 36; i++) invItems[i] = inv.get(i);

        let remaining = item.count;

        const priceCandidates = invItems
            .map((v, i) => ({ v, i }))
            .filter(({v}) => equal(v, item))
            .sort((a, b) => a.v.count - b.v.count);

        for (const { v: currItem, i } of priceCandidates) {
            if (currItem.count > remaining) {
                inv.set(i, { ...currItem, count: currItem.count - remaining });
                remaining = 0;
                break;
            }
            else {
                remaining -= currItem.count;
                inv.set(i, undefined);
            }
        }

        return remaining;
    }
    export function takeAll(inv: Inventory, item: Item): number {
        const invItems: ItemStack[] = [];
        for (let i = 0; i < 36; i++) invItems[i] = inv.get(i);

        let count = 0;

        for (let i = 0; i < 36; i++) {
            if (equal(inv.get(i), item)) {
                count += inv.get(i).count;
                inv.set(i, undefined);
            }
        }

        return count;
    }

    export function tryGive(inv: Inventory, item: ItemStack) {
        const tmp = inv.clone();

        if (give(tmp, item) !== 0) return false;
        inv.copyFrom(tmp);

        return true;
    }
    export function tryTake(inv: Inventory, item: ItemStack) {
        const tmp = inv.clone();

        if (take(tmp, item) !== 0) return false;
        inv.copyFrom(tmp);

        return true;
    }
}

class GamePlayer {
    public readonly name: string;
    public readonly uuid: string;
    public player?: ServerPlayer;
    public timer = 0;
    public tiers: Record<string, number> = {};

    private _team?: GameTeam;
    private _state: State = undefined!;

    private _pendingGives: { tier: ToolTier, i: number }[] = [];

    public get state() { return this._state; }
    public get team() { return this._team; }

    public set team(newTeam: GameTeam | undefined) {
        if (this.team == newTeam) return;

        const oldTeam = this.team;

        oldTeam?.players?.delete(this);
        newTeam?.players?.add(this);
        this._team = newTeam;

        if (oldTeam != null) {
            for (const other of oldTeam.players) {
                if (other != this) this.player?.sendMessage(`${this.name} left your team.`);
                else this.player?.sendMessage(`You left team ${oldTeam?.team.name}`);
            }
        }
        if (newTeam != null) {
            for (const other of newTeam.players) {
                if (other != this) this.player?.sendMessage(`${this.name} joined your team.`);
                else this.player?.sendMessage(`You joined team ${newTeam?.team.name}`);
            }
        }

        if (newTeam != null && this.game.running) this.setState(State.Dead);
        else this.setState(State.Spectator);
    }

    private _refreshNativeState() {
        if (this.player == null) return;

        this.player.health = 20;

        if (this.state === State.Alive) this.player.gamemode = 'survival';
        else this.player.gamemode = 'spectator';

        if (this.state === State.Alive) this.player.location = this.team!.team.spawn;
        else if (this.state !== State.Dead) this.player.location = this.game.config.spawn;
    }

    private _updateInv() {
        const inv = this.player?.inventory;
        if (inv == null) return;
        if (this.state === State.Spectator) return;

        if (this.state !== State.Alive) {
            inv.clear();
            return;
        }

        for (const { i, tier } of this._pendingGives) {
            InvUtils.give(inv, tier.tiers[i].item);
        }

        this._pendingGives = [];

        for (const tier of this.game.config.tiers) {
            if (!(tier.id in this.tiers)) {
                if (tier.forceFirst) this.tiers[tier.id] = 0;
                else continue;
            }
            const tierI = this.tiers[tier.id];

            switch (tier.type) {
                case 'armor':
                    for (let i = 0; i <= tierI; i++) {
                        const tierItem = tier.tiers[i];
                        if (tierItem.boots != null) inv.set(36, tierItem.boots);
                        if (tierItem.leggings != null) inv.set(37, tierItem.leggings);
                        if (tierItem.chestplate != null) inv.set(38, tierItem.chestplate);
                        if (tierItem.helmet != null) inv.set(39, tierItem.helmet);
                    }
                    break;
                case 'tool': {
                    // let highest: number | undefined;
                    for (let i = tier.tiers.length - 1; i >= 0; i--) {
                        const tierItem = tier.tiers[i];

                        const allItems = [
                            ...(this.player?.inventory ?? []),
                            this.player?.screen?.cursorStack
                        ];
                        const hasItem = allItems.find(v => InvUtils.equal(v, tierItem.item)) != null;

                        if (hasItem) {
                            if (this.tiers[tier.id] < i) this.tiers[tier.id] = i;
                        }

                        if (!tierItem.multiple) {
                            if (this.tiers[tier.id] === i && !hasItem) {
                                InvUtils.give(inv, tierItem.item);
                                // highest = i;
                            }
                            else if (i !== this.tiers[tier.id] && hasItem) {
                                InvUtils.takeAll(inv, tierItem.item);
                            }
                        }

                        continue;
                    }
                }
            }
        }
    }

    private _onDeath() {
        for (const tier of this.game.config.tiers) {
            if (!(tier.id in this.tiers)) continue;
            const tierI = this.tiers[tier.id];
            const tierEl = tier.tiers[tierI];

            switch (tierEl.onDeath) {
                case 'decrease':
                    this.tiers[tier.id]--;
                    break;
                case 'reset':
                    delete this.tiers[tier.id];
                    break;
                case undefined:
                case null:
                    break;
                default:
                    this.tiers[tier.id] = tierEl.onDeath;
            }

            if (tier.type === 'tool' && tier.id in this.tiers) {
                this._pendingGives.push({ tier, i: this.tiers[tier.id] });
            }
        }
    }

    public setState(state: State, force = false, silent = force) {
        if (this.team == null) state = State.Spectator;
        if (this.state === state) return;
        if (this.state === State.Alive && state !== State.Alive) this._onDeath();
        this._state = state;

        if (state === State.Spectator) this.tiers = {};

        if (!silent) {
            if (state === State.Dead) this.game.sendMessage(`${this.name} died.`);
            if (state === State.Eliminated) this.game.sendMessage(`${this.name} was eliminated.`);
            if (this.player == null) return;
        }

        if (this.state === State.Dead) this.timer = this.game.config.respawnTimeout;

        this._refreshNativeState();

        if (this.team == null) return;

        if (state === State.Eliminated && ![ ...this.team.players ].find(v => v.state === State.Alive)) {
            this.team.setState(State.Eliminated, force, silent);
        }
    }

    public kill() {
        if (!this.game.running) return;
        if (this.team == null) return;

        if (this.team.state !== State.Alive) this.setState(State.Eliminated);
        else this.setState(State.Dead);
    }

    public tick() {
        if (this.player == null) return;

        this._updateInv();

        if (!this.game.running) return;

        if (this.player.location.y < this.game.config.yKillPlane) {
            this.kill();
            this.player.location = this.game.config.spawn;
        }

        if (this.state === State.Dead) {
            if (this.timer < 0) this.setState(State.Alive);
            else this.sendTitle({
                title: 'You died!',
                subtitle: `Respawning in ${this.timer.toFixed(2)} seconds...`,
                duration: 0.1
            });
        }

        this.timer -= 1 / 20;
        this.player.foodLevel = 20;
        this.player.saturation = 20;
    }

    public sendMessage(msg: string) {
        if (this.player == null) return;
        this.player.sendMessage(msg);
    }
    public sendTitle(title: TitleConfig) {
        if (this.player == null) return;
        this.player.sendTitle(title);
    }

    public onInvClick(slotI: number, button: number, screen: InventoryScreen, action: ActionType) {
        const slot = screen.getSlot(slotI);
        const inventories = screen.inventories;

        let movedOutside = false;
        let movedItem: ItemStack | undefined;

        switch (action) {
            case 'swap':
                movedItem = this.player?.inventory?.get(button);

                if (slot.inventory !== this.player?.inventory) {
                    movedOutside = true;
                }
                break;
            case 'pickup':
                movedItem = screen.cursorStack;
                if (slot.inventory !== this.player?.inventory && movedItem != null) movedOutside = true;
                break;
            case 'pickupAll':
                movedItem = slot.item;
                if (slot.inventory === this.player?.inventory && movedItem != null && inventories.length > 1) {
                    movedOutside = true;
                }
                break;
            case 'quickMove':
                movedItem = slot.item;
                if (slot.inventory === this.player?.inventory && movedItem != null && inventories.length > 1) {
                    movedOutside = true;
                }
                break;
            case 'throw':
                movedItem = slot.item;
                movedOutside = slot.inventory === this.player?.inventory;
                break;
            case 'clone': return true;
            case 'quickCraft':
                if (button === 1 || button === 5) {
                    movedItem = screen.cursorStack;
                    movedOutside = slot.inventory !== this.player?.inventory;
                }
                else return true;
        }

        for (const tier of this.game.config.tiers) {
            if (!(tier.id in this.tiers)) continue;
            const tierI = this.tiers[tier.id];

            switch (tier.type) {
                case 'armor':
                    if (slotI < 36 || slotI >= 40) continue;

                    for (let i = 0; i < tierI; i++) {
                        const tierItem = tier.tiers[i];
                        if (tierItem.boots != null && slotI === 36) return false;
                        if (tierItem.leggings != null && slotI === 37) return false;
                        if (tierItem.chestplate != null && slotI === 38) return false;
                        if (tierItem.helmet != null && slotI === 39) return false;
                    }

                    continue;
                case 'tool':
                    const tierItem = tier.tiers[tierI];
                    if (!InvUtils.equal(tierItem.item, movedItem)) continue;
                    if (tierItem.multiple) continue;
                    if (movedOutside) return false;
                    continue;
            }
        }

        return true;
    }

    public giveTier(id: string, i: number) {
        const tier = this.game.config.tiers.find(v => v.id === id);
        if (tier == null) return false;

        let ok = false;

        if (tier.type === 'tool' && tier.tiers[i].multiple) {
            this._pendingGives.push({ i, tier });
            ok = true;
        }

        if (!(id in this.tiers) || this.tiers[id] < i) {
            this.tiers[id] = i;
            return true;
        }
        else return ok;
    }

    public constructor(public readonly game: Game, player: ServerPlayer) {
        this.player = player;
        this.uuid = player.uuid;
        this.name = player.name;

        this.setState(State.Spectator, true);
    }
}

class GameTeam {
    public readonly players = new Set<GamePlayer>();
    public readonly gens = new Set<GameGenerator>();

    private _state: State = undefined!;
    private _bedBlocks: Array<{ loc: Location, block: BlockState }> = [];

    public get state() { return this._state; }

    private _removeBed() {
        const breakBlock = (loc: Location) => {
            const block = this.game.world.getBlock(loc);
            if (!BED_REGEX.test(block.id)) return;

            this.game.blocks.onBreak(loc);
            this._bedBlocks.push({ loc, block })
            this.game.world.setBlock(loc, { id: 'air' }, false);

            // A bed may be on the end of the scan region
            breakBlock(loc.add(1, 0, 0));
            breakBlock(loc.add(-1, 0, 0));
            breakBlock(loc.add(0, 0, 1));
            breakBlock(loc.add(0, 0, -1));
        }

        const bedLoc = this.team.bed;
        const radius = 2;

        for (let x = bedLoc.x - radius; x < bedLoc.x + radius; x++) {
            for (let y = bedLoc.y - radius; y < bedLoc.y + radius; y++) {
                for (let z = bedLoc.z - radius; z < bedLoc.z + radius; z++) {
                    breakBlock(new Location(x, y, z));
                }
            }
        }
    }
    private _restoreBed() {
        for (const { loc, block } of this._bedBlocks) {
            this.game.world.setBlock(loc, block, false);
        }

        this._bedBlocks = [];
    }

    public setState(state: State, force = false, silent = force) {
        if (this._state === state) return;

        this._state = state;

        switch (state) {
            case State.Alive:
                for (const player of this.players) {
                    if (player.state === State.Eliminated || player.state === State.Spectator) {
                        player.setState(State.Alive, force, silent);
                    }
                }

                this._restoreBed();

                break;
            case State.Dead:
                for (const player of this.players) {
                    if (player.state === State.Eliminated || player.state === State.Spectator) {
                        player.setState(State.Alive, force, silent);
                    }
                }
                if (!silent) {
                    this.game.sendMessage(`Team ${this.team.name}'s bed was broken!`);
                    this.sendTitle({
                        title: 'Your bed was broken!', subtitle: 'You will no longer respawn',
                        duration: 2, fadeIn: .5, fadeOut: .5,
                    });
                }
                this._removeBed();

                break;
            case State.Eliminated:
                for (const player of this.players) player.setState(State.Eliminated, force, silent);
                if (!silent) this.game.sendMessage(`Team ${this.team.name} was eliminated!`);
                this._removeBed();

                break;
            case State.Spectator:
                for (const player of this.players) player.setState(State.Spectator, force, silent);
                this._restoreBed();
                break;
        }
    }

    public sendMessage(msg: string) {
        for (const player of this.players) {
            player.sendMessage(msg);
        }
    }
    public sendTitle(title: TitleConfig) {
        for (const player of this.players) {
            player.sendTitle(title);
        }
    }

    public tick() {
        for (const gen of this.gens) gen.tick();
    }

    public constructor(
        public readonly game: Game,
        public readonly team: Team,
    ) {
        for (const gen of team.gens) {
            this.gens.add(new GameGenerator(game, gen));
        }
    }
}

class GameShop {
    public readonly screen: InventoryScreen;
    public readonly inventory: Inventory;

    private _border = { id: 'gray_stained_glass_pane', count: 1, display: { Name: '""' } };
    private _selected = { id: 'white_stained_glass_pane', count: 1, display: { Name: '""' } };
    private _close = { id: 'barrier', count: 1, display: { Name: '"Close"' } };
    private _hotshop: Hotshop = [];
    private _hotshopPath: string;

    private _pageI = 0;

    private _getTier(id: string) {
        const res =  this.game.config.tiers.find(v => v.id === id);
        if (res == null) throw "Misconfigured game!";
        return res;
    }

    private _getCategory(i = this._pageI): ShopCategory | undefined {
        if (i === 0) {
            const res: ShopCategory = {
                icon: { id: 'nether_star', display: { Name: '"Quick Shop"' }, count: 1 },
                items: new Array<ShopItem | undefined>(27),
            }

            for (let i = 0; i < this._hotshop.length; i++) {
                const hotshopEl = this._hotshop[i];

                if (hotshopEl.length === 2) {
                    const shopItem = this.game.config.shop.categories[hotshopEl[0]].items[hotshopEl[1]];
                    res.items[i] = shopItem;
                }
            }

            return res;
        }
        else return this.game.config.shop.categories[i - 1];
    }
    private _getItems(category: ShopCategory): (ItemStack | undefined)[] {
        const res = new Array<ItemStack | undefined>(27);

        for (let i = 0; i < category.items.length; i++) {
            const shopItem = category.items[i];
            let item: any | undefined;

            switch (shopItem?.type) {
                case 'item':
                    item = shopItem.item;
                    break;
                case 'tier': {
                    if (typeof shopItem.icon === 'object') item = shopItem;
                    else {
                        const tier = this._getTier(shopItem.tier);
                        switch (tier.type) {
                            case 'armor':
                                item = (tier.tiers[shopItem.i] as any)[['boots', 'leggings', 'chestplate', 'helmet'][shopItem.icon]];
                                break;
                            case 'tool':
                                item = tier.tiers[shopItem.i].item;
                                break;
                        }
                    }
                    break;
                }
            }

            item = { ...item, count: 1 };

            item.display ??= {};
            item.display.Lore ??= [];
            item.display.Lore.push({ color: 'light_gray', text: 'Click to buy' });

            res[i] = item;
        }

        return res;
    }

    private _rebuild() {
        this.inventory.clear();

        for (let i = 0; i < 9; i++) {
            this.inventory.set(i + 9, this._border);
            this.inventory.set(i + 9 * 5, this._border);

            const category = this._getCategory(i);
            if (category == null) continue;
            this.inventory.set(i, category.icon);
        }

        this.inventory.set(4 + 9 * 5, this._close);
        this.inventory.set(this._pageI + 9, this._selected);

        let category = this._getCategory();

        if (category == null) {
            this.selectPage(0);
            category = this._getCategory()!;
        }

        const items = this._getItems(category);
        for (let i = 0; i < 27; i++) {
            this.inventory.set(i + 9 * 2, items[i]);
        }
    }

    private async _loadHotshop() {
        switch ((await Filesystem.stat(this._hotshopPath)).type) {
            case "file": {
                const f = await Filesystem.open(this._hotshopPath, 'rw');
                let raw = JSON.parse(Encoding.decode(await f.read(await f.length())));
                await f.close();

                if (Array.isArray(raw)) {
                    this._hotshop = raw.map(v => {
                        if (!Array.isArray(v)) return [];
                        if (v.length > 2) v.length = 2;
                        if (v.length < 2) v.length = 0;
                        if (typeof v[0] !== 'number' || typeof v[1] !== 'number') return [];
                        return [ Number(v[0]), Number(v[1]) ];
                    });

                    this._rebuild();
                    return;
                }
            }
            case "folder":
                await Filesystem.rm(this._hotshopPath, true);
            default:
                this._hotshop = this.game.config.shop.defaultHotshop;
                await this._saveHotshop();
        }

    }
    private async _saveHotshop() {
        try { await Filesystem.mkdir('/shops'); } catch {}
        try { await Filesystem.mkfile(this._hotshopPath); } catch {}

        const f = await Filesystem.open(this._hotshopPath, 'w');

        await f.write(Encoding.encode(JSON.stringify(this._hotshop)));
        await f.close();
    }

    private _giveItem(shopItem: ShopItem, playerInv: Inventory) {
        if (!InvUtils.tryGive(playerInv, shopItem.item)) return "Items can't fit in your inventory.";
    }
    private _giveTier(shopItem: ShopTier, playerInv: Inventory) {
        const gp = this.game.getPlayer(this.player);
        const tier = this.game.config.tiers.find(v => v.id === shopItem.tier && this.game.config.playerTiers.includes(v.id));

        if (tier == null) return "Misconfigured game, contact an admin!";
        if (gp == null) return "You must be in a game to purchase this!";

        if (!gp.giveTier(shopItem.tier, shopItem.i)) return "Couldn't buy that item!";
    }

    private _takePrice(shopItem: ShopBuyable, playerInv: Inventory) {
        if (!InvUtils.tryTake(playerInv, shopItem.price)) return "Not enough materials to purchase that.";
    }

    public selectPage(i: number) {
        this._pageI = i;
        if (this._pageI > this.game.config.shop.categories.length + 1) this._pageI = this.game.config.shop.categories.length;
        this._rebuild();
    }
    public close() {
        this.player.closeInventory();
    }
    public buy(i: number) {
        const shopItem = this._getCategory()?.items?.[i];
        const playerInv = this.player.inventory.clone();

        if (shopItem == null) return;

        let msg = this._takePrice(shopItem, playerInv);

        if (msg != null) {
            this.player.sendMessage(msg);
            return;
        }

        switch (shopItem.type) {
            case 'item': msg = this._giveItem(shopItem, playerInv); break;
            case 'tier': msg = this._giveTier(shopItem, playerInv); break;
        }

        if (msg != null) {
            this.player.sendMessage(msg);
            return;
        }

        this.player.inventory.copyFrom(playerInv);
    }

    public onClick(slot: number, button: number, action: ActionType) {
        if (action !== 'pickup' || slot >= 54) return;

        const x = Math.floor(slot % 9);
        const y = Math.floor(slot / 9);

        if (y === 0) this.selectPage(x);
        else if (y >= 2 && y <= 4) {
            this.buy(x + (y - 2) * 9);
        }
        else if (y === 5) {
            if (x === 4) this.close();
        }
    }

    public constructor(
        public readonly game: Game,
        public readonly player: ServerPlayer
    ) {
        this.inventory = new Inventory(6 * 9);
        this.screen = player.openInventory('Shop', '9x6', this.inventory);
        this._hotshopPath = `/shops/${player.uuid}`;

        this._loadHotshop();
    }
}

class GameShopkeepers {
    private _uuids = new Set<string>();

    private async _loadUuids() {
        log('test');
        const path = `/shopkeepers`;
        const stat = await Filesystem.stat(path);

        if (stat.type === 'folder') await Filesystem.rm(path, true);
        if (stat.type !== 'file') await Filesystem.mkfile(path);

        const f = await Filesystem.open(path, 'rw');
        const raw = Encoding.decode(await f.read(await f.length()));
        await f.close();

        this._uuids.clear();
        for (const el of raw.split('\n').map(v => v.trim()).filter(v => v !== '')) {
            this._uuids.add(el);
        }
    }
    private async _saveUuids() {
        const path = `/shopkeepers`;
        const f = await Filesystem.open(path, 'rw');
        await f.write(Encoding.encode([ ...this._uuids ].join('\n')));
        await f.close();
    }

    public has(uuid: string) {
        return this._uuids.has(uuid);
    }
    public add(uuid: string) {
        this._uuids.add(uuid);
        this._saveUuids();
    }

    public create(player: ServerPlayer) {
        const loc = new Location(
            Math.floor(player.location.x) + .5,
            Math.floor(player.location.y),
            Math.floor(player.location.z) + .5,
        );
        const nbt = {
            id: 'villager',
            NoAI: true,
            NoGravity: true,
            Invulberable: true,
            PersistenceRequired: true,
            Silent: true,
            VillagerData: {
                level: 99
            },
        };
        const entity = player.world.summon(loc, nbt);
        this.add(entity.uuid);
    }
    public destroy(player: ServerPlayer) {
        const villagers = [ ...this._uuids ]
            .map(v => this.game.server.getByUUID(v))
            .filter(v => v != null)
            .sort((a, b) => b.location.distance(player.location) - a.location.distance(player.location));

        if (villagers.length > 0) {
            this._uuids.delete(villagers[0].uuid);
            villagers[0].discard();
        }
    }

    public onEntityUse(player: ServerPlayer, entity: Entity) {
        if (this.has(entity.uuid)) {
            this.game.openShop(player);
            return false;
        }
        else return true;
    }
    public onEntityDamage(entity: Entity) {
        return !this.has(entity.uuid);
    }

    public constructor(
        public readonly game: Game
    ) {
        this._loadUuids();
    }
}

class GameGenerator {
    private _timer = 0;
    private _items = new Set<Entity>();

    public tick() {
        for (const { freq, item } of this.gen.items) {
            if (freq - this._timer % freq < .05) {
                const e = this.game.world.summon(
                    this.gen.loc,
                    { id: 'item', Item: { ...item, Count: item.count } }
                );
                this._items.add(e);
            }
        }
        this._timer += 1 / 20;
    }
    public reset() {
        for (const item of this._items) item.discard();

        this._timer = 0;
        this._items.clear();
    }

    public constructor(
        public readonly game: Game,
        public readonly gen: Gen,
    ) { }
}

class GameBlockManager {
    private readonly _placed = new Set<string>();
    private readonly _broken = new Map<string, BlockState>();

    public onPlace(loc: Location) {
        const prev = this.game.world.getBlock(loc);
        const strLoc = `${loc.x} ${loc.y} ${loc.z}`;

        if (prev.id !== 'minecraft:air') {
            this._broken.set(strLoc, prev);
        }

        this._placed.add(strLoc);

        return true;
    }
    public onBreak(loc: Location) {
        return this._placed.delete(`${loc.x} ${loc.y} ${loc.z}`);
    }

    public reset() {
        for (const strLoc of this._placed) {
            const loc = new Location(...strLoc.split(' ').map(v => Number(v)) as [number, number, number]);
            this.game.world.setBlock(loc, { id: 'air' });
        }
        for (const [ strLoc, state ] of this._broken) {
            const loc = new Location(...strLoc.split(' ').map(v => Number(v)) as [number, number, number]);
            this.game.world.setBlock(loc, state);
        }
    }

    public constructor(
        public readonly game: Game
    ) { }
}

class GameCountdown {
    private _timer?: number;
    private _coef?: number;

    private _sendTitle() {
        if (this._timer == null) return;

        let title: TitleConfig | undefined = {
            title: `Game starts in ${this._timer.toFixed(2)} seconds...`
        };

        if (this._timer > 30) {
            if (Math.abs(this._timer % 10) < .01) title = { ...title, duration: 2, fadeIn: .1, fadeOut: .1 };
            else title = undefined;
        }
        else if (this._timer > 15) {
            if (Math.abs(this._timer % 5) < .01) title = { ...title, duration: 2, fadeIn: .1, fadeOut: .1 };
            else title = undefined;
        }
        else if (this._timer > 5) {
            if (Math.abs(this._timer % 1) < .01) title = { ...title, duration: 1.1, fadeIn: 0, fadeOut: 0 };
            else title = undefined;
        }
        else title = { ...title, duration: 1.1, fadeIn: 0, fadeOut: 0 };

        if (title != null) this.game.sendTitle(title);
    }

    public tick() {
        const teams = this.game.teams;

        const coef = teams.filter(v => v.players.size > 0).length / teams.length;
        const conf = this.game.config.countdowns.find(v => coef <= v.coef);

        if (conf != null && conf.coef !== this._coef) {
            this._coef = conf.coef;
            if (conf.time < 0) {
                this._timer = undefined;
                this.game.sendTitle({
                    title: 'Game cancelled',
                    subtitle: 'Waiting for more players',
                    duration: 2, fadeIn: .1, fadeOut: .1
                });
            }
            else this._timer = conf.time;
        }

        if (this._timer != null) {
            if (this._timer < 0) this.game.start();
            else {
                this._sendTitle();
                this._timer -= 1 / 20;
            }
        }
    }

    public reset() {
        this._timer = undefined;
        this._coef = undefined;
    }

    public constructor(
        public readonly game: Game
    ) { }
}

class Game {
    private _players: GamePlayer[] = [];
    private _teams: GameTeam[] = [];
    private _gens: GameGenerator[] = [];
    private _shops = new Map<InventoryScreen, GameShop>();

    private _running = false;
    private _won = false;

    public readonly blocks: GameBlockManager;
    public readonly countdown: GameCountdown;
    public readonly shopkeepers: GameShopkeepers;

    private _checkWinCond(stop = true) {
        if (!this.running || this._won) return;

        const inGameTeams = this._teams.filter(v => v.state === State.Alive || v.state === State.Dead);

        if (inGameTeams.length < 2) {
            if (inGameTeams.length < 1) {
                this.sendTitle({
                    title: "Draw", subtitle: "Nobody won",
                    fadeIn: 0.1, duration: 1, fadeOut: 0.1
                });
            }
            else {
                const team = inGameTeams[0];

                for (const player of this._players) {
                    if (player.team === team) player.sendTitle({
                        title: "You won!",
                        fadeIn: 0.1, duration: 1, fadeOut: 0.1
                    });
                    else player.sendTitle({
                        title: "You lost!", subtitle: `Team ${team.team.name} won.`,
                        fadeIn: 0.1, duration: 1, fadeOut: 0.1
                    });
                }
            }

            this._won = true;

            if (stop) this.stop();
        }
    }

    public get running() {
        return this._running;
    }

    public get players() {
        return [...this._players];
    }
    public get teams() {
        return [...this._teams];
    }
    public get gens() {
        return [...this._gens];
    }

    public getPlayer(player: ServerPlayer | string) {
        const uuid = typeof player === 'string' ? player : player.uuid;
        return this._players.find(v => v.uuid === uuid);
    }

    public sendMessage(msg: string) {
        for (const player of this._players) {
            player.sendMessage(msg);
        }
    }
    public sendTitle(title: TitleConfig) {
        for (const player of this._players) {
            player.sendTitle(title);
        }
    }

    public start() {
        if (this.running) return;

        this._running = true;
        this._won = true;

        for (const player of this._players) {
            if ((player.team?.players.size ?? 0) > 0) player.setState(State.Alive, true);
            else player.setState(State.Spectator, true);
        }

        for (const team of this._teams) {
            if (team.players.size > 0) team.setState(State.Alive, true);
            else team.setState(State.Spectator, true);
        }

        this._won = false;

        this._checkWinCond();
    }
    public stop() {
        if (!this.running) return;

        this._checkWinCond(false);

        this._running = false;
        this._won = false;

        for (const team of this._teams) {
            team.setState(State.Spectator, false, true);
            for (const gen of team.gens) gen.reset();
        }
        for (const player of this._players) {
            player.player?.inventory?.clear();
            player.setState(State.Spectator, false, true);
        }

        for (const gen of this.gens) gen.reset();

        this.blocks.reset();
        this.countdown.reset();
    }

    public onLogin(player: ServerPlayer) {
        let gamePlayer = this.getPlayer(player);

        if (gamePlayer != null) {
            if (!this.running) this._players = this._players.filter(v => v.uuid !== player.uuid);
            else {
                gamePlayer.player = player;
                gamePlayer.setState(State.Dead, true);
                return;
            }
        }

        gamePlayer = new GamePlayer(this, player);
        this._players.push(gamePlayer);
        gamePlayer.setState(State.Spectator, true);

        let candidates = this._teams.filter(v => v.players.size < this.config.teamSize).sort((a, b) => b.players.size - a.players.size);

        if (candidates.length === 0) return;

        candidates = candidates.filter(v => v.players.size === candidates[0].players.size);

        const team = candidates[Math.floor(candidates.length * Math.random())];

        gamePlayer.team = team;

        return team;
    }
    public onLogoff(player: ServerPlayer) {
        const gp = this.getPlayer(player);
        if (gp == null) return;

        if (this.running) {
            gp.player = undefined;
            gp.setState(State.Dead);
        }
        else {
            gp.team = undefined;
            this._players.splice(this._players.indexOf(gp), 1);
        }
    }

    public onDeath(player: ServerPlayer) {
        this.getPlayer(player)?.kill();
    }

    public onEntityDamage(entity: Entity) {
        return this.shopkeepers.onEntityDamage(entity);
    }
    public onEntityUse(player: ServerPlayer, entity: Entity) {
        return this.shopkeepers.onEntityUse(player, entity);
    }

    public onUpdateConfig() {
        this.stop();
        const tmpPlayers = this._players.map(v => v.player!).filter(v => v != null);

        this._teams.length = 0;
        this._gens.length = 0;
        this._players.length = 0;

        this._teams.push(...this.config.teams.map(v => new GameTeam(this, v)));

        for (const player of tmpPlayers) {
            this.onLogin(player);
        }

        for (const gen of this.config.gens) {
            this._gens.push(new GameGenerator(this, gen));
        }
    }

    public onTick() {
        for (const player of this._players) player.tick();

        if (this._running) {
            for (const team of this._teams) team.tick();
            for (const gen of this._gens) gen.tick();

            this._checkWinCond();
        }
        else {
            this.countdown.tick();
        }
    }

    public onBlockPlace(loc: Location) {
        if (!this.running) return true;
        return this.blocks.onPlace(loc);;
    }
    public onBlockBreak(loc: Location, player: ServerPlayer) {
        if (!this.running) return true;

        const prev = this.world.getBlock(loc);
        const gp = this.getPlayer(player);

        if (this.running && BED_REGEX.test(prev.id)) {
            for (const team of this._teams) {
                if (team.team.bed.distance(loc) < 2) {
                    if (gp?.team === team) gp.sendMessage("You can't break your bed.");
                    else team.setState(State.Dead);

                    return false;
                }
            }
        }
        else return this.blocks.onBreak(loc);
    }

    public onInvClick(screen: InventoryScreen, player: ServerPlayer, slot: number, button: number, action: ActionType) {
        if (this._shops.has(screen)) {
            this._shops.get(screen)?.onClick(slot, button, action);
            return false;
        }

        const gp = this.getPlayer(player);
        if (gp != null) {
            if (!gp.onInvClick(slot, button, screen, action)) return false;
        }

        return true;
    }
    public onInvClose(screen: InventoryScreen) {
        this._shops.delete(screen);
        return true;
    }

    public openShop(player: ServerPlayer) {
        const shop = new GameShop(this, player);
        this._shops.set(shop.screen, shop);
    }

    constructor(
        public readonly config: Config,
        public readonly world: ServerWorld,
        public readonly server: Server,
    ) {
        this.blocks = new GameBlockManager(this);
        this.countdown = new GameCountdown(this);
        this.shopkeepers = new GameShopkeepers(this);
        this.onUpdateConfig();
    }
}

serverLoad.on(server => {
    const config = new Config();
    const game = new Game(config, server.worlds[0], server);

    server.registerCommand('bw', {
        execute(_args, sender, sendInfo, sendError) {
            const args = _args.split(' ').map(v => v.trim()).filter(v => v !== '');

            switch (args[0]) {
                case 'start':
                    game.start();
                    sendInfo("Started the game!");
                    break;
                case 'stop':
                    game.stop();
                    sendInfo("Stopped the game!");
                    break;
                case 'break-bed':
                    const team = game.teams.find(v => v.team.name === args[1]);
                    if (team != null) team.setState(State.Dead);
                    break;
                case 'shop': {
                    game.openShop(sender as ServerPlayer);
                    break;
                }
                case 'villager':
                case 'shopkeeper':
                case 'sk':
                case 'v':
                    if (!(sender instanceof ServerPlayer)) {
                        sendError("Only players may create/destoy shopkeepers");
                        return;
                    }
                    switch (args[1]) {
                        case 'c':
                        case 'create':
                            game.shopkeepers.create(sender);
                            break;
                        case 'd':
                        case 'destroy':
                        case 'delete':
                        case 'rm':
                            game.shopkeepers.destroy(sender);
                            break;
                    }
                    break;
                default:
                    sendError("Invalid command");
            }
        },
    });

    server.playerJoin.on(player => game.onLogin(player));
    server.playerLeave.on(player => game.onLogoff(player));
    server.entityDamage.on((cancel, entity, damage) => {
        if (!game.onEntityDamage(entity)) cancel();
        else if (entity instanceof ServerPlayer && damage >= entity.health) {
            game.onDeath(entity);
            cancel();
        }
    });
    server.tickEnd.on(() => {
        game.onTick();
    });
    server.blockBreak.on((cancel, loc, player) => {
        if (!game.onBlockBreak(loc, player)) cancel();
    });
    server.blockPlace.on((cancel, loc) => {
        if (!game.onBlockPlace(loc)) cancel();
    });
    server.inventoryScreenClicked.on((cancel, screen, player, slot, button, action) => {
        if (!game.onInvClick(screen, player, slot, button, action)) cancel();
    });
    server.inventoryScreenClosed.on((cancel, screen) => {
        if (!game.onInvClose(screen)) cancel();
    });
    server.entityUse.on((cancel, player, entity) => {
        if (!game.onEntityUse(player, entity)) cancel();
    });
});

