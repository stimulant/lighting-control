#region usings
using System;
using System.ComponentModel.Composition;

using VVVV.PluginInterfaces.V1;
using VVVV.PluginInterfaces.V2;
using VVVV.Utils.VColor;
using VVVV.Utils.VMath;

using VVVV.Core.Logging;
#endregion usings
using System.Net; 
using System.IO;
using System.Text;
using System.Net.Sockets; 

namespace VVVV.Nodes
{
	#region PluginInfo
	[PluginInfo(Name = "ColorToOPC", Category = "Color", Help = "Basic template with one color in/out", Tags = "", AutoEvaluate = true)]
	#endregion PluginInfo
	public class ColorColorToOPCNode : IPluginEvaluate
	{
		#region fields & pins
		[Input("Colors", DefaultColor = new double[] {
			0.1,
			0.2,
			0.3,
			1.0
		})]
		public ISpread<RGBAColor> FInput;
		
		[Input("Host", IsSingle = true, DefaultString = "")]
		public ISpread<string> FHost;
		
		[Input("Port", IsSingle = true)]
		public ISpread<int> FPort;

		[Import()]
		public ILogger Flogger;
		#endregion fields & pins
		
		string host = "localhost";
		int port = 0;
		TcpClient tcpClient = null;
		
		public void Connect(string h, int p)
		{
			try {
				Flogger.Log(LogType.Debug, "Trying to connect");
				host = h; port = p;
				if (tcpClient != null)
					tcpClient.Close();
				
				tcpClient = new TcpClient();
		        tcpClient.Connect(host, port);
				
				Flogger.Log(LogType.Debug, "Connected");
			}
			catch (Exception e) {
				Flogger.Log(LogType.Debug, "Connection error" + e.ToString());
            	tcpClient = null;
        	}
		}

		public void Evaluate(int SpreadMax)
		{
			if (host != FHost[0] || port != FPort[0])
				Connect(FHost[0], FPort[0]);
			
			// we have a tcp client, send our colors via OPC
			if (tcpClient != null)
			{
				Stream stm = tcpClient.GetStream();
				
				int numBytes = 3 * FInput.SliceCount;
    			int packetLen = 4 + numBytes;
				byte[] packetData = new byte[packetLen];
				packetData[0] = 0;  // Channel
      			packetData[1] = 0;  // Command (Set pixel colors)
      			packetData[2] = (byte)(numBytes >> 8);
      			packetData[3] = (byte)(numBytes & 0xFF);
				
				for (int i=0; i < FInput.SliceCount; i++)
				{
					packetData[i*3 + 4] = (byte)((int)(FInput[i].R * 256) >> 16);
    				packetData[i*3 + 5] = (byte)((int)(FInput[i].G * 256) >> 8);
    				packetData[i*3 + 6] = (byte)((int)(FInput[i].B * 256));
				}
				stm.Write(packetData, 0, packetData.Length);
			}
		}
	}
}
