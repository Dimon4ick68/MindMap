```mermaid
classDiagram
    direction LR
    
    MindMap "1" -- "1" Node : has root (Composition)
    Node "1" o-- "*" Node : has children (Composite)
    
    class MindMap {
        +String mapId
        +String title
        +Node rootNode
    }
    
    class Node {
        +String id
        +String text
        +Point position
        +List~Node~ children
        +INodeRenderer renderer
        +void addChild(Node node)
        +void removeChild(Node node)
    }

    class INodeRenderer {
        <<Interface>>
        +void render(GraphicsContext gc, Node node)
    }
    
    Node o-- INodeRenderer : has (Bridge)

    TextNodeRenderer ..|> INodeRenderer : implements
    ImagePreviewRenderer ..|> INodeRenderer : implements
    VideoPreviewRenderer ..|> INodeRenderer : implements

    class MindMapCanvas {
       +ILineDrawingStrategy lineStrategy
       +void setStrategy(ILineDrawingStrategy s)
       +void drawMap()
    }

    class ILineDrawingStrategy {
        <<Interface>>
        +void drawConnection(GraphicsContext gc, Node start, Node end)
    }

    MindMapCanvas o-- ILineDrawingStrategy : uses (Strategy)

    StraightLineStrategy ..|> ILineDrawingStrategy : implements
    CurvedLineStrategy ..|> ILineDrawingStrategy : implements