declare interface Event<T extends (...args: any[]) => void> {
    on(handle: T): () => void;
}
declare type CancelEvent<T extends (...args: any[]) => void> = Event<T extends (...args: infer ArgT) => any ? (cancel: () => void, ...args: ArgT) => void : T>;
declare interface CommandHandle {
    execute(args: string, sender: Entity | undefined, sendInfo: (text: any) => void, sendError: (text: any) => void): void;
}
declare interface TitleConfig {
    title?: string;
    subtitle?: string;
    fadeIn?: number;
    fadeOut?: number;
    duration?: number;
}

declare type NbtTypeFromKey<T extends string> =
    T extends `${string}$${'l'|'i'|'s'|'f'|'d'}` ? (number | number[]) :
    T extends `${string}$${'b'}` ? (number | boolean | number[]) :
    (NbtElement | NbtElement[]);

declare type NbtElement = NbtCompound | number | string | boolean;

declare interface NbtCompound {
    [key: string]: NbtElement | NbtElement[];
}


declare interface DamageSource {
    damager?: Entity;
    location?: Location;
    type: string;
}

declare interface ItemStack extends Item {
    count: number;
}

declare interface Item extends NbtCompound {
    id: string;
}

declare class Location {
    readonly x: number;
    readonly y: number;
    readonly z: number;

    add(...locations: [...([Location] | [number, number, number])]): Location;
    subtract(...locations: [...([Location] | [number, number, number])]): Location;

    dot(location: Location): Location;
    distance(loc: Location): number;

    dot(x: number, y: number, z: number): number;
    distance(x: number, y: number, z: number): number;
    length(): number;

    setX(x: number): Location;
    setY(y: number): Location;
    setZ(z: number): Location;

    toString(): `[${number} ${number} ${number}]`;

    constructor(x: number, y: number, z: number);
    constructor(val: Location);
}

declare interface BlockState {
    id: string;
    [prop: string]: string | number | boolean;
}

declare class ServerWorld {
    setBlock(loc: Location, state: BlockState, update?: boolean): void;
    getBlock(loc: Location): BlockState;
    summon(loc: Location, nbt: NbtCompound & { id: string; }): Entity;
}

declare class Server {
    registerCommand(name: string, handle: CommandHandle): void;
    cmd(command: string, opts?: { at?: Location, pitch?: number, yaw?: number, as?: Entity, world?: ServerWorld }): { output: string[], code: number };
    sendMessage(text: string): void;

    getByUUID(uuid: string): Entity;

    readonly tickStart: Event<() => void>;
    readonly tickEnd: Event<() => void>;

    readonly blockBreak: CancelEvent<(loc: Location, player: ServerPlayer) => void>;
    readonly blockPlace: CancelEvent<(loc: Location, player: ServerPlayer) => void>;

    readonly playerJoin: Event<(player: ServerPlayer) => void>;
    readonly playerLeave: Event<(player: ServerPlayer) => void>;

    readonly entityDamage: CancelEvent<(entity: Entity, damage: number, source: DamageSource) => void>;
    readonly entityUse: CancelEvent<(player: ServerPlayer, entity: Entity) => void>;

    readonly itemUse: CancelEvent<(item: ItemStack, player: ServerPlayer, hand: 'main' | 'off') => void>;

    readonly inventoryScreenClicked: CancelEvent<(screen: InventoryScreen, player: ServerPlayer, index: number, button: number, action: ActionType) => void>;
    readonly inventoryScreenClosed: CancelEvent<(screen: InventoryScreen, player: ServerPlayer) => void>;

    readonly players: ServerPlayer[];
    readonly worlds: ServerWorld[];

    static maxStack(id: string): number;
}

declare class Entity {
    location: Location;
    name: string;
    readonly uuid: string;
    readonly world: ServerWorld;

    discard(): void;
}
declare class LivingEntity extends Entity {
    health: number;
    readonly maxHealth: number;
}

declare type Gamemode = 'survival' | 'creative' | 'adventure' | 'spectator';
declare type InvType = '3x3' | '9x1' | '9x2' | '9x3' | '9x4' | '9x5' | '9x6';
declare type ActionType = 'clone' | 'pickup' | 'pickupAll' | 'quickCraft' | 'quickMove' | 'swap' | 'throw';

declare class Player extends LivingEntity {
    sendMessage(text: any): void;
}

declare class ServerPlayer extends Player {
    readonly inventory: Inventory;
    readonly screen: InventoryScreen | undefined;

    gamemode: Gamemode;
    foodLevel: number;
    saturation: number;
    sendTitle(title: TitleConfig): void;

    openInventory(name: string, type: InvType, inv: Inventory): InventoryScreen;
    closeInventory(): void;
}

declare class Inventory implements Iterable<ItemStack> {
    readonly size: number;

    get(i: number): ItemStack;
    set(i: number, item?: ItemStack): void;
    clear(): void;

    clone(): Inventory;
    copyFrom(inv: Inventory): void;

    [Symbol.iterator](): Iterator<ItemStack>;

    constructor(n: number);
}

declare interface Slot {
    readonly i: number;
    readonly x: number;
    readonly y: number;
    readonly inventory: Inventory;

    get item(): ItemStack;
    set item(val: ItemStack);
}

declare class InventoryScreen {
    readonly inventories: Inventory[];
    readonly id: number;
    readonly cursorStack: ItemStack | undefined;
    getSlot(i: number): Slot;
}

declare var serverLoad: Event<(server: Server) => void>;
