import boto3
import sys
import argparse

def fetch_ips(env_group, region='us-east-2'):
    ec2 = boto3.client('ec2', region_name=region)
    response = ec2.describe_instances(Filters=[{'Name': 'instance-state-name', 'Values': ['running']}])

    instances_info = []

    for reservation in response['Reservations']:
        for instance in reservation['Instances']:
            instance_name = next((tag['Value'] for tag in instance.get('Tags', []) if tag['Key'] == 'Name'), None)
            if env_group == 'DT':
                if instance_name and 'BASTION' in instance_name:
                    public_ip = instance.get('PublicIpAddress', 'N/A')
                    private_ip = instance.get('PrivateIpAddress', 'N/A')
                    instances_info.append((instance_name, public_ip, private_ip))
            else:
                if instance_name and instance_name.startswith(env_group):
                    public_ip = instance.get('PublicIpAddress', 'N/A')
                    private_ip = instance.get('PrivateIpAddress', 'N/A')
                    instances_info.append((instance_name, public_ip, private_ip))

    return sorted(instances_info, key=lambda x: x[0])

def print_table(instances_info):
    if not instances_info:
        print("No running instances found.")
        return

    print(f"{'Instance Name':<30} {'Public IP':<20} {'Private IP':<20}")
    print("-" * 70)

    for instance_name, public_ip, private_ip in instances_info:
        print(f"{instance_name:<30} {public_ip:<20} {private_ip:<20}")

def main():
    parser = argparse.ArgumentParser(description="Getting_Parameters")
    print("Call recieved from pipeline")
    parser.add_argument('-e', '--EnvironmentGroup', action='store', type=str, required=True, help="Environment Group")
    parser.add_argument('-i', '--InstanceAttribute', action='store', type=str, required=True, help="Instance Attribute")
    
    args = parser.parse_args()
    env_group = args.EnvironmentGroup
    instance_attribute = args.InstanceAttribute
    print("value of instance attribute ", instance_attribute)

    if instance_attribute != 'fetch_ip':
        print("Invalid instance attribute.")
        sys.exit(1)

    instances_info = fetch_ips(env_group)
    print_table(instances_info)

if __name__ == '__main__':
    main()
