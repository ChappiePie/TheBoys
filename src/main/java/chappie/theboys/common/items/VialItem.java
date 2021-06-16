package chappie.theboys.common.items;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import xyz.heroesunited.heroesunited.hupacks.HUPacks;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

public class VialItem extends Item {
    private static final Random rand = new Random();

    public VialItem(Properties propertiesIn) {
        super(propertiesIn);
    }

    @Override
    public void fillItemCategory(ItemGroup itemGroup, NonNullList<ItemStack> items) {
        if (this.allowdedIn(itemGroup)) {
            items.add(new ItemStack(this));
            items.add(setInjection(new ItemStack(this), "compound_v"));

            for (ResourceLocation superpower : getAbilities()) {
                items.add(setInjection(new ItemStack(this), superpower.toString()));
            }
        }

    }

    public static int getColor(ItemStack stack, int color) {
        final int id = rand.nextInt(DyeColor.values().length);
        if (color > 0 || StringUtils.isNullOrEmpty(getInjection(stack))) {
            return 16777215;
        }

        if (getInjection(stack).equals("compound_v")) {
            return 6009838;
        }
        return Lists.newArrayList(DyeColor.values()).get(id).getColorValue();
    }

    public static String getInjection(ItemStack stack) {
        return stack.getOrCreateTag().getString("Injection");
    }

    public static ItemStack setInjection(ItemStack stack, String injection) {
        stack.getOrCreateTag().putString("Injection", injection);
        return stack;
    }

    protected List<ResourceLocation> getAbilities() {
        IResourceManager manager = HUPacks.getInstance().getResourceManager();
        List<ResourceLocation> list = Lists.newArrayList();
        if (manager == null) return list;
        int i = "husuperpowers".length() + 1;

        for(ResourceLocation resourcelocation : manager.listResources("husuperpowers", (p_223379_0_) -> p_223379_0_.endsWith(".json"))) {
            String s = resourcelocation.getPath();
            ResourceLocation resourcelocation1 = new ResourceLocation(resourcelocation.getNamespace(), s.substring(i, s.length() - ".json".length()));

            try (IResource iresource = manager.getResource(resourcelocation);
                    InputStream inputstream = iresource.getInputStream();
                    Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))) {
                JsonElement jsonelement = JSONUtils.fromJson(HUPacks.GSON, reader, JsonElement.class);
                if (jsonelement != null) {
                    list.add(resourcelocation1);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
            }
        }

        return list;
    }
}