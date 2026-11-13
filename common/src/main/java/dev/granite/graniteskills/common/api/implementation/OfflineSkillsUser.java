package dev.granite.graniteskills.common.api.implementation;

import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.TraitModifier;
import dev.granite.graniteskills.api.user.SkillsUser;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class OfflineSkillsUser implements SkillsUser {

    private final GraniteSkillsPlugin plugin;
    private final UUID uuid;

    public OfflineSkillsUser(GraniteSkillsPlugin plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public double getSkillXp(Skill skill) {
        return 0;
    }

    @Override
    public void addSkillXp(Skill skill, double amountToAdd) {

    }

    @Override
    public void addSkillXp(Skill skill, double amountToAdd, XpSource source) {

    }

    @Override
    public void addSkillXpRaw(Skill skill, double amountToAdd) {

    }

    @Override
    public void setSkillXp(Skill skill, double amount) {

    }

    @Override
    public int getSkillLevel(Skill skill) {
        return plugin.config().getStartLevel();
    }

    @Override
    public void setSkillLevel(Skill skill, int level) {

    }

    @Override
    public void setSkillLevel(Skill skill, int level, boolean refresh) {

    }

    @Override
    public double getSkillAverage() {
        return 0;
    }

    @Override
    public double getStatLevel(Stat stat) {
        return 0;
    }

    @Override
    public double getBaseStatLevel(Stat stat) {
        return 0;
    }

    @Override
    public double getMana() {
        return 0;
    }

    @Override
    public double getMaxMana() {
        return 0;
    }

    @Override
    public void setMana(double mana) {

    }

    @Override
    public boolean consumeMana(double amount) {
        return false;
    }

    @Override
    public int getPowerLevel() {
        return 0;
    }

    @Override
    public void addStatModifier(StatModifier statModifier) {

    }

    @Override
    public void removeStatModifier(String name) {

    }

    @Override
    public @Nullable StatModifier getStatModifier(String name) {
        return null;
    }

    @Override
    public Map<String, StatModifier> getStatModifiers() {
        return new ConcurrentHashMap<>();
    }

    @Override
    public void addTraitModifier(TraitModifier traitModifier) {

    }

    @Override
    public void removeTraitModifier(String name) {

    }

    @Override
    public @Nullable TraitModifier getTraitModifier(String name) {
        return null;
    }

    @Override
    public Map<String, TraitModifier> getTraitModifiers() {
        return null;
    }

    @Override
    public double getEffectiveTraitLevel(Trait trait) {
        return 0;
    }

    @Override
    public double getBonusTraitLevel(Trait trait) {
        return 0;
    }

    @Override
    public int getAbilityLevel(Ability ability) {
        return 0;
    }

    @Override
    public int getManaAbilityLevel(ManaAbility manaAbility) {
        return 0;
    }

    @Override
    public Locale getLocale() {
        return plugin.getDefaultLanguage();
    }

    @Override
    public boolean hasSkillPermission(Skill skill) {
        return true;
    }

    @Override
    public Set<Skill> getJobs() {
        return Set.of();
    }

    @Override
    public void addJob(Skill job) {

    }

    @Override
    public void removeJob(Skill job) {

    }

    @Override
    public void clearAllJobs() {

    }

    @Override
    public int getJobLimit() {
        return 0;
    }

    @Override
    public void sendActionBar(String message) {

    }

    @Override
    public void sendActionBar(String message, int duration, TimeUnit timeUnit) {

    }

    @Override
    public void pauseActionBar(int duration, TimeUnit timeUnit) {

    }

    @Override
    public void sendAbilityMessage(String message) {

    }

    @Override
    public CompletableFuture<Boolean> save(boolean removeFromMemory) {
        var future = new CompletableFuture<Boolean>();
        future.complete(false);
        return future;
    }

}
