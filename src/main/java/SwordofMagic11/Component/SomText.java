package SwordofMagic11.Component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.io.Serializable;
import java.util.List;

public class SomText implements Cloneable, Serializable {

    private TextComponent.Builder builder = Component.empty().toBuilder();

    public static SomText create(String text) {
        return new SomText().add(text);
    }

    public static SomText create(Component component) {
        return new SomText().add(component);
    }

    public static SomText create() {
        return new SomText();
    }

    private SomText() {

    }

    public SomText add(String text) {
        builder.append(Component.text(text));
        return this;
    }

    public SomText add(SomText text) {
        builder.append(text.toComponent());
        return this;
    }

    public SomText add(Component component) {
        builder.append(component);
        return this;
    }

    public SomText addDeco(String text) {
        builder.append(Component.text("§6======§r " + text + " §6======"));
        return this;
    }

    public SomText addHover(String text, String hoverText) {
        return addHover(text, SomText.create(hoverText));
    }

    public SomText addHover(String text, List<String> hoverText) {
        SomText hover = SomText.create();
        for (String str : hoverText) {
            hover.add(str);
        }
        return addHover(text, hover);
    }

    public SomText addHover(String text, SomText hoverText) {
        builder.append(Component.text(text).hoverEvent(HoverEvent.showText(hoverText.toComponent())));
        return this;
    }

    public SomText addClickEvent(String text, SomText hoverText, String command, ClickEvent.Action action) {
        TextComponent runCommand = Component.text(text);
        runCommand = runCommand.hoverEvent(HoverEvent.showText(hoverText.toComponent()));
        runCommand = runCommand.clickEvent(ClickEvent.clickEvent(action, command));
        builder.append(runCommand);
        return this;
    }

    public SomText addRunCommand(String text, String hoverText, String command) {
        return addRunCommand(text, SomText.create(hoverText), command);
    }

    public SomText addRunCommand(String text, SomText hoverText, String command) {
        return addClickEvent(text, hoverText, command, ClickEvent.Action.RUN_COMMAND);
    }

    public SomText addSuggestCommand(String text, String hoverText, String command) {
        return addSuggestCommand(text, SomText.create(hoverText), command);
    }

    public SomText addSuggestCommand(String text, SomText hoverText, String command) {
        return addClickEvent(text, hoverText, command, ClickEvent.Action.SUGGEST_COMMAND);
    }

    public SomText addClipboard(String text, String hoverText, String command) {
        return addClipboard(text, SomText.create(hoverText), command);
    }

    public SomText addClipboard(String text, SomText hoverText, String command) {
        return addClickEvent(text, hoverText, command, ClickEvent.Action.COPY_TO_CLIPBOARD);
    }

    public SomText addOpenURL(String text, String hoverText, String url) {
        return addOpenURL(text, SomText.create(hoverText), url);
    }

    public SomText addOpenURL(String text, SomText hoverText, String url) {
        return addClickEvent(text, hoverText, url, ClickEvent.Action.OPEN_URL);
    }

    public SomText newLine() {
        builder = builder.appendNewline();
        return this;
    }

    public Component toComponent() {
        return builder.build();
    }

    @Override
    public SomText clone() {
        try {
            SomText clone = (SomText) super.clone();
            clone.builder = builder;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
