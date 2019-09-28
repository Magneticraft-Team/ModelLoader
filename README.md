# ModelLoader

ModelLoader is Minecraft library that allows mods to use 3D 
models in several formats like [glTF](https://www.khronos.org/gltf/)
and mcx.

ModelLoader works with Minecraft 1.12 and Minecraft 1.14 under 
[Minecraft Forge](https://github.com/MinecraftForge/MinecraftForge).

## Examples
There are examples for common use cases in the repo 
[ModelLoaderExample](https://github.com/Magneticraft-Team/ModelLoaderExample).
Some of the examples are: simple block, block with rotations, 
block with TileEntity renderer, simple item, item with animations.

### Usage from a mod
There are 2 events ModelRegisterEvent and ModelRetrieveEvent.

- ModelRegisterEvent is fired in the mod event bus before models 
and textures are loaded. This event has a method called 
`registerModel` where you can add your models. Each model is 
identified by a ModelResourceLocation and has a ModelConfig.
To use a model for a block or a item, you need to match the 
ModelResourceLocation of the model with the ModelResourceLocation
of the BlockState/Item.

- ModelRetrieveEvent is fired in the mod event bus after all 
model are ready. This event has a method called `getModel` that
allow you to get the IBakedModel from the model id. Also there 
is a method called `getAnimations` that lets you get the
animations to be rendered in a TileEntityRenderer or in a 
ItemStackTileEntityRenderer.

Each registered model has a ModelConfig with the following 
properties:

- location: ResourceLocation of the model to load
- itemTransforms: The transformations to apply to the model when
is in the ground, the gui, the first person hand, third person 
hand, etc.
- rotation: ModelRotation to apply to the model before baking.
- bake: whether or not to generate a IBakedModel, should be true
if the item is going to be used for an item or a simple block
- animate: whether or not to load animations from the model
- itemRenderer: marks the model so minecraft uses an 
ItemStackTileEntityRenderer instead of the IBakedModel.
- preBake: function called before the model is baked, 
allow you to alter the original model data.
- postBake: function called after the model is baked,
allows you to alter the IBakedModel or provide your own.

Additionally you can use glTF and mcx file in blockstate json files
if you register your mod with 
`MLCustomModelLoader.registerDomain(MOD_ID);`

## Supported Formats
Currently the library supports glTF and mcx. 
You can register your own formats with 
`ModelFormatRegistry.registerHandler(extension, handler);`

### glTF
glTF (GL Transmission Format) is free specification for 3D scenes 
and models made by Khronos Group, the creators of OpenGl, Vulkan 
and WebGL. 

This format uses a Json header file with most of the model settings 
and several binary files to store heavy model parts like vertices, 
UV coordinates, keyframes, etc. This combination make the model
mostly human readable while keeping the models small.

One of the main reasons to supports this format, apart from 
the great adoption by 3D tools, is the fact that allows animations.
Currently this library supports keyframe animations with translation,
rotation and scale channels. The format also allows skeletal 
animations but it not implemented yet, if you want this feature 
write a issue.

There are several editors that support this format like blender 
or my own [Modeler](https://github.com/cout970/Modeler).

There is also a Animation builder if you want to create your 
animations in code. Every animated model is made of nodes and 
each node has a transformation thar you can edit to animate 
it manually 

### MCX
MCX is simple json format, with similar structure to obj, but 
with properties directly mapped to minecraft models. Its simple 
and easy to edit manually but doesn't support animations and it's 
not as disk-space efficient as other formats. The only editor that
supports this format is my own 
[Modeler](https://github.com/cout970/Modeler).

## Technical Support
If you have any question or you need help with the library you can
contact me on my [discord channel](https://discord.gg/zzEhXWD).
I will respond when I get some time, but there is more people in the 
channel that can help you faster if I'm not available.
 
  
