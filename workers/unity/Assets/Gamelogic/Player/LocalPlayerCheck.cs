using Improbable.Unity.Visualizer;
using Player;
using UnityEngine;

namespace Assets.Gamelogic.Player
{
    class LocalPlayerCheck : MonoBehaviour
    {

        public static LocalPlayerCheck LocalPlayer {get; private set; }
        [Require] private LocalPlayer.Writer LocalPlayerState;

        public void OnEnable()
        {
            LocalPlayer = this;
        }
    }
}
