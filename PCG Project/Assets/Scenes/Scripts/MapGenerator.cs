using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MapGenerator : MonoBehaviour
{
    private int[,] map =
    {
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 1, 1, 0, 1, 1, 1, 0, 0},
        {0, 0, 0, 1, 0, 1, 0, 1, 0, 0},
        {0, 0, 0, 1, 1, 1, 0, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
        {0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    [SerializeField] private GameObject wall;
    [SerializeField] private GameObject floor;

    // Start is called before the first frame update
    void Start()
    {
        GenerateMap();
    }

    void GenerateMap()
    {
        float mapWidth = map.GetLength(1);
        float mapHeight = map.GetLength(0);

        Vector2 startPos = new Vector2(-mapWidth / 2f + 0.5f, mapHeight / 2f - 0.5f);

        for (int y = 0; y < map.GetLength(0); y++)
        {
            for (int x = 0; x < map.GetLength(1); x++)
            {
                GameObject tile = map[y, x] == 1 ? floor : wall;
                float tileSize = 1f;
                Vector2 pos = new Vector2(startPos.x + x * tileSize, startPos.y - y * tileSize);    // Generate from the center of the camera
                Instantiate(tile, pos, Quaternion.identity);
            }
        }
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
