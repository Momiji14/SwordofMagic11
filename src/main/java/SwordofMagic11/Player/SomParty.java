package SwordofMagic11.Player;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomText;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Map.MapData;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static SwordofMagic11.Component.Function.*;

public class SomParty {
    public static final String Prefix = "§b[P]§r";
    public static final ConcurrentHashMap<String, SomParty> partyList = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<PlayerData, SomParty> inviteList = new ConcurrentHashMap<>();

    public static boolean hasInvite(PlayerData playerData) {
        return inviteList.containsKey(playerData);
    }

    public static void accept(PlayerData playerData) {
        if (inviteList.containsKey(playerData)) {
            SomParty party = inviteList.get(playerData);
            party.addMember(playerData);
            inviteList.remove(playerData);
        } else {
            playerData.sendMessage(Prefix + "§b招待§aがありません", SomSound.Nope);
        }
    }

    private final String id;
    private List<String> lore = new ArrayList<>();
    private final List<PlayerData> members = new ArrayList<>();
    private PlayerData leader;
    private int limit = 4;
    private boolean bossRepeat = false;

    private Material icon = Material.PAPER;
    public boolean entryBoard = false;

    public SomParty(String id, PlayerData playerData) {
        this.id = id;
        leader = playerData;
        members.add(playerData);
        partyList.put(id, this);
        playerData.setParty(this);
        playerData.sendMessage(Prefix + "§e" + id + "§aを作成しました");
    }

    public String getId() {
        return id;
    }

    public List<PlayerData> getMember() {
        return members;
    }

    public PlayerData getLeader() {
        return leader;
    }

    public void setLeader(PlayerData leader) {
        this.leader = leader;
        sendMessage(leader.getDisplayName() + "§aが§eパーティリーダー§aなりました", SomSound.Tick);
    }

    public void sendMessage(String message, SomSound sound) {
        for (PlayerData member : members) {
            member.sendMessage(Prefix + message, sound);
        }
    }

    public void invitePlayer(PlayerData inviter, PlayerData playerData) {
        if (playerData.hasParty()) {
            inviter.sendMessage(Prefix + playerData.getName() + "§aはすでに§e" + playerData.getParty().getId() + "§aに§b参加§aしています", SomSound.Nope);
        } else if (inviteList.containsKey(playerData)) {
            inviter.sendMessage(Prefix + playerData.getName() + "§aはすでに§e" + inviteList.get(playerData).getId() + "§aから§b招待§aを受けています", SomSound.Nope);
        } else {
            inviteList.put(playerData, this);
            sendMessage(inviter.getName() + "§aが§r" + playerData.getName() + "§aを§b招待§aしました", SomSound.Tick);
            playerData.sendMessage(SomText.create().addRunCommand(inviter.getName() + "§aから§e" + id + "§aに§b招待§aされました §e[/party accept]", "§eクリック§aで§eパーティ§aに§b参加", "/party accept"));
        }
    }

    public void addMember(PlayerData member) {
        members.add(member);
        member.setParty(this);
        sendMessage(member.getName() + "§aが§b参加§aしました", SomSound.Tick);
    }

    public void removeMember(PlayerData member) {
        members.remove(member);
        member.setParty(null);
        member.sendMessage(Prefix + "§e" + id + "§aを§c脱退§aしました", SomSound.Tick);
        sendMessage(member.getName() + "§aが§c脱退§aしました", SomSound.Tick);
        if (members.isEmpty()) {
            partyList.remove(id);
            for (MapData mapData : MapDataLoader.getMapDataList()) {
                mapData.deletePrivateInstance(this);
            }
            member.sendMessage(Prefix + "§e" + id + "§aが§c解散§aしました");
        } else {
            if (leader == member) setLeader(randomGet(members));
            if (member.isInPrivateIns()) {
                member.teleport(member.lastSpawnLocation());
                boolean anyHas = false;
                for (PlayerData playerData : members) {
                    if (playerData.donation().hasPrivateInsRight()) {
                        anyHas = true;
                    }
                }
                if (!anyHas) {
                    for (PlayerData other : members) {
                        if (other.isInPrivateIns()) {
                            CustomLocation location = other.getLocation();
                            location.setWorld(other.getMap().getGlobalInstance(false));
                            other.teleport(location);
                            other.sendMessage("§dプライベートインスタンス§aの§e権利所持者§aが§c脱退§aしました", SomSound.Tick);
                        }
                    }
                }
            }
        }

    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public void setLore(String lore) {
        setLore(loreText(lore.replace("&n", "\n")));
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public boolean hasLore() {
        return !lore.isEmpty();
    }

    public void setEntryBoard(boolean entryBoard) {
        this.entryBoard = entryBoard;
    }

    public boolean isEntryBoard() {
        return entryBoard;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public void setBossRepeat(boolean bossRepeat) {
        this.bossRepeat = bossRepeat;
    }

    public boolean isBossRepeat() {
        return bossRepeat;
    }

    public List<String> viewText() {
        List<String> lore = new ArrayList<>();
        lore.add(decoText(id));
        if (this.lore.isEmpty()) {
            lore.add("§c説明文未設定");
        } else {
            lore.addAll(this.lore);
        }
        lore.add(decoSeparator("メンバー"));
        lore.add("§7・§e" + leader.getName());
        for (PlayerData member : members) {
            if (member != leader) {
                lore.add("§7・§r" + member.getDisplayName());
            }
        }
        return lore;
    }

    public CustomItemStack viewItem() {
        CustomItemStack item = new CustomItemStack(icon);
        List<String> lore = viewText();
        item.setDisplay(lore.get(0));
        lore.remove(0);
        item.addLore(lore);
        if (isEntryBoard()) {
            item.addSeparator("募集情報");
            item.addLore(decoLore("募集枠") + (getLimit() > getMember().size() ? getLimit() - getMember().size() + "枠" : "人数上限"));
        }

        item.setCustomData("Party", id);
        return item;
    }
}
